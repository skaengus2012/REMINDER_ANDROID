---
name: issue-to-pr
description: >-
  Runs the whole issue-to-Draft-PR flow from one invocation (`/issue-to-pr <issue-number>`): pre-start questions,
  requirements analysis, human review gate 1, plan creation, human review gate 2, then a dynamic workflow
  (`plan-to-pr`) that implements the plan with TDD, self-tests, runs a bounded review discussion with a separate
  reviewer, verifies, and opens a Draft PR. Use this skill whenever the user wants to take a GitHub issue all the way
  to a pull request, start working on an issue end to end, or says things like "이슈 처음부터 PR까지 진행해줘" /
  "이슈 886 시작해줘", even if they do not mention 'issue-to-pr'.
---

# Issue to PR Skill

## Overview

Connects the existing single-step skills into one flow. Human review gates run here, in the main session, because a
dynamic workflow cannot ask the user anything while it runs. The long unattended part is delegated to the workflow
`plan-to-pr` (`workflows/plan-to-pr.js` next to the `skills/` folder).

```
Questions -> Requirements (subagent) -> Gate 1 -> Plan (subagent) -> Gate 2 -> Workflow `plan-to-pr`
                                                                              implement -> review loop -> verify -> Draft PR
```

The user performs the final verification on the Draft PR. This skill never marks a PR Ready and never merges it.

## Project Config

Everything that depends on this project is collected here so the skill can later be promoted to a shared repository.
The rest of this document refers to these values by name.

| Key | Value |
|-----|-------|
| `specDir` | `docs/spec/issue<N>/` (`requirements.md`, `plan.md`, optional `mockup/`) |
| `workflowPath` | `.agents/workflows/plan-to-pr.js` |
| `testCommand` | `./gradlew :<module>:test` per changed module |
| `fullTestCommand` | `./gradlew test` (build-logic or other cross-cutting changes) |
| `uiQaTool` | Maestro (MCP tools `mcp__maestro__*`) |
| `defaultRounds` | `2` |
| `requirementsSkillPath` | `~/.claude/skills/create-gh-issue-analysis-report/SKILL.md` |
| `planSkillPath` | `~/.claude/skills/create-plans/SKILL.md` |
| `tddSkillPath` | `~/.claude/skills/test-driven-development/SKILL.md` |
| `reviewSkillPath` | `~/.claude/skills/requesting-code-review/SKILL.md` |
| `verifySkillPath` | `~/.claude/skills/verification-before-completion/SKILL.md` |
| `prSkillPath` | `.agents/skills/create-pr/SKILL.md` |
| `branchRulesPath` | `docs/guidelines/branch-rules.md` |
| `commitRulesPath` | `docs/guidelines/commit-rules.md` |
| `specRulesPath` | `docs/guidelines/spec-docs.md` |

## Rules for Every Subagent

- Subagents cannot ask the user anything. Tell each one: if something is ambiguous, do not guess; return it as a
  question and stop.
- Pass explicit file paths (the `*Path` values above) in addition to relying on skill matching. Expand a leading
  `~` to the absolute home directory first, since a tool may not resolve it.
- Pass paths, not pasted document contents.

---

## Step 1. Identify the Issue and the Working Branch

Take the issue number from the argument. If it is missing, extract `issue<N>` from the current branch name. If neither
works, ask the user; never guess a number.

Then check the working branch with `git branch --show-current` against `<branchRulesPath>`. Do this whether or not a
PR will be created, because Step 7 and the workflow both commit on the current branch. The branch is acceptable when
it is `feature/issue<N>` or `epic/issue<N>` for this issue, or a worktree branch (`worktree/...`), whose remote name
is settled later in Step 7-2.

Otherwise (for example `master`, `develop`, or another issue's branch), stop before anything is written or committed
and ask with `AskUserQuestion`:
- **Create `feature/issue<N>` from the current branch** (Recommended when the current branch is the intended parent)
- **Switch to an existing branch** (ask which one)
- **Continue on the current branch anyway** (the user accepts that commits land there)

Never commit the spec documents or the implementation to `master` or `develop` unless the user chose to continue.

## Step 2. Ask Before Starting

Always ask, in a single `AskUserQuestion` call, even if the user seems to have answered earlier:

1. **Start point** (`AskUserQuestion` allows at most 4 options): check which of `<specDir>/requirements.md` and
   `<specDir>/plan.md` exist and offer only the points that apply:
   - From the beginning (requirements analysis)
   - Review the existing requirements (Gate 1); the plan is created or updated after approval
   - Review the existing plan (Gate 2)
   - Run the workflow with the existing approved plan
2. **Max code-review discussion rounds**: `1`, `2` (default, Recommended), `3`.
3. **Create the Draft PR**: yes / no. If no, the workflow stops after final verification, leaving local commits only.
4. **Reviewer model**: `opus` (default, Recommended), `sonnet`, or the session default (`inherit`).

Keep the answers; they are passed to the workflow in Step 7.

## Step 3. Requirements Analysis

Skip when the chosen start point is later than this step.

Launch a subagent (`Agent`) with a prompt like:

> Read `<requirementsSkillPath>` and follow it for GitHub issue #<N>; also read `<specRulesPath>`. You cannot ask the
> user anything: do not guess, list every decision you need under Open Questions. Write or update
> `<specDir>/requirements.md`. Reply with: the file path, the number of FR/NFR items, and the Open Questions that need
> a user decision.

If the subagent reports a critical ambiguity that blocks the report, stop: show the questions to the user, ask for the
answers, and rerun the subagent with them. Never fill the gap yourself.

## Step 4. Gate 1: Review the Requirements

Skip when the chosen start point is later than this step (reviewing the existing plan, or running the workflow).

Show the user the path, the FR/NFR counts and the Open Questions, then ask with `AskUserQuestion`:
**Approve** / **Request changes**.

- **Request changes**: collect the change request, launch the same subagent again with the request (update in place,
  per the skill's rules for an existing report), and ask again. Repeat until approved.
- Open Questions that remain at approval are recorded as decisions the user accepted; carry them to the plan step.

## Step 5. Plan Creation

Skip when the chosen start point is later than this step.

Launch a subagent with a prompt like:

> Read `<planSkillPath>` and follow it for GitHub issue #<N>. Read `<specDir>/requirements.md` and, if it exists,
> `<specDir>/mockup/`. You cannot ask the user anything: do not guess, return unresolved decisions as questions
> instead of writing them as facts. The plan must include a test strategy in Verification: `unit` (unit tests only,
> scoped to changed modules, the default) or `unit+ui` (also automated UI QA with `<uiQaTool>`), choosing `unit+ui`
> only when the planned changes touch Compose or layout UI. Write the choice into `plan.md` as the literal line
> `testStrategy: unit` or `testStrategy: unit+ui` under Verification (update it in place if `plan.md` already
> exists). Write `<specDir>/plan.md`. Reply with: the file path, the task count, the `testStrategy` value, and the
> questions that need a user decision.

If questions come back, ask the user (one `AskUserQuestion` call, grouped), then rerun the subagent with the answers.

## Step 6. Gate 2: Review the Plan

Same loop as Gate 1 for `plan.md`: show the path, task count, test strategy and open questions; **Approve** /
**Request changes**; on changes rerun the plan subagent with the request and ask again until approved. Skip this loop
when the chosen start point is running the workflow with the existing approved plan.

Always read the approved `testStrategy` from the literal `testStrategy:` line in `plan.md`, even when the loop above
was skipped, and not from memory, so it is also correct when the run started at Gate 2 or at the workflow. If the line is missing or unclear, ask the user which
strategy to use; never assume one.

## Step 7. Prepare and Run the Workflow

### 7-1. Commit the spec documents

The workflow starts from a clean tree, because `<prSkillPath>` stops on a dirty one. If `<specDir>` has uncommitted
files, commit them following `<commitRulesPath>` (e.g. `Docs #<N> - Add requirements and plan`). Then run
`git status --porcelain`: if anything else is left, show it and ask the user how to handle it (commit, keep out of
the way, or discard are the user's call); do not start the workflow until the tree is clean. Finally record
`startCommit` with `git rev-parse HEAD`.

### 7-2. Resolve what `create-pr` would ask (only when creating the PR)

The workflow cannot ask questions, but `<prSkillPath>` asks about the base branch and the remote branch name. Decide
both now, using its Step 1 and Step 2 rules (read it):

- `remoteBranch`: the current branch if it matches `feature/issue<N>` / `epic/issue<N>`, otherwise ask the user
  (recommend `feature/issue<N>`).
- `baseBranch`: the parent branch when it is `master`, `develop` or `epic/*`; otherwise ask the user.

State both in one line before continuing. If the user chose not to create the PR, pass `null` for both.

### 7-3. Run the workflow

Call the `Workflow` tool with `scriptPath: "<workflowPath>"` and `args` as a JSON object (not a string):

```json
{
  "issue": 886,
  "rounds": 2,
  "createPr": true,
  "reviewerModel": "opus",
  "testStrategy": "unit",
  "startCommit": "<sha>",
  "baseBranch": "master",
  "remoteBranch": "feature/issue886",
  "config": {
    "specDir": "<specDir with N substituted>",
    "testCommand": "<testCommand>",
    "fullTestCommand": "<fullTestCommand>",
    "uiQaTool": "<uiQaTool>",
    "tddSkillPath": "<tddSkillPath>",
    "reviewSkillPath": "<reviewSkillPath>",
    "verifySkillPath": "<verifySkillPath>",
    "prSkillPath": "<prSkillPath>",
    "commitRulesPath": "<commitRulesPath>",
    "branchRulesPath": "<branchRulesPath>",
    "specRulesPath": "<specRulesPath>"
  }
}
```

Fill `config` from the Project Config table with `<N>` substituted. Do not add `dryRun` unless testing the workflow
itself.

## Step 8. Report

The workflow returns `{ status, stage, reason, prUrl, headCommit, unresolved, suggestions, uiQa,
manualUiCheckRequired, calls }`. Report to the user, in Korean:

- `status: "pr_created"`: the PR URL, unresolved review points (also written in the PR body), and a reminder that the
  PR is a Draft for the user to verify and mark Ready.
- `status: "committed"`: the user chose no PR; list the local commits and what was verified.
- `status: "aborted"`: the `stage` and `reason`, what was left on the branch, and the question or failure the user
  must resolve. Do not retry on your own.
- Always mention `manualUiCheckRequired` when true.

---

## Common Mistakes

1. **Putting a gate inside the workflow**: the workflow cannot ask. Gates stay in this skill.
2. **Guessing through ambiguity**: stop and report instead of choosing an interpretation.
3. **Passing a stringified `args`**: pass a real JSON object, otherwise the script receives one string.
4. **Skipping the pre-start questions** because the answers seem obvious. Always ask.
5. **Launching the workflow before the spec documents are committed**: `create-pr` stops on a dirty tree.
6. **Retrying an aborted workflow silently**: report the failure first.
