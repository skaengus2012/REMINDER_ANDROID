---
name: create-pr
description: >-
  Creates a Draft pull request for the current branch following the project's branch and commit rules.
  Validates the branch/commit conventions, picks the correct base branch (master or epic), runs tests for the
  changed modules, drafts the title and body from the linked GitHub issue and commit log, then pushes and opens
  a Draft PR with `gh`. Use this skill whenever the user asks to create, open, or submit a pull request, push
  the work for review, or says things like "PR 올려줘" / "PR 만들어줘", even if they do not mention 'create-pr'.
---

# Create PR Skill

## Overview

Opens a **Draft** pull request for the work on the current branch. A Draft is used because pushing and opening a PR
is visible to others; the user reviews the result on GitHub and decides when to mark it Ready for review.
This skill never marks a PR Ready and never merges it.

Read these before starting, since the checks below are derived from them:
- `docs/guidelines/branch-rules.md` (branch naming and which branch a PR targets)
- `docs/guidelines/commit-rules.md` (commit format and types)

---

## Step 1. Inspect the Context

```bash
git branch --show-current
git status --short
git log --format='%h %s' origin/master..HEAD   # adjust the base after Step 2
```

- **Issue number**: extract `issue<N>` from the branch name (e.g. `feature/issue790` -> `790`).
  If the branch has no issue number, ask the user for it, or use `N/A` when there is none.
- **Uncommitted changes**: if `git status` is not clean, stop and ask whether to commit them first
  (follow `commit-rules.md`). A PR built from a dirty tree would not match what the user sees locally.
  If the user agrees, propose a commit message (`<Type> #<N> - <subject>`, plus a body for `Implement`) and commit
  before continuing. If there are no commits ahead of the base branch after that, there is nothing to open a PR for.
- **Branch name**: it should be `feature/issue<N>` or `epic/issue<N>`. If it does not match (e.g. a worktree
  branch like `worktree/feature/issue790`), ask the user which remote branch name to push to, recommending
  `feature/issue<N>`. Pushing the local name as is would put a rule-breaking branch on the remote.

## Step 2. Decide the Base Branch

The user always chooses the base branch. Git does not reliably record which branch a worktree branch was created
from (the reflog often says only `Created from HEAD`), so guessing silently risks a noisy diff and a merge into the
wrong place. Collect candidates, then ask.

**Collect candidates in this order** (this is also the order to present them):

1. **Parent branch (recommended)**: the branch the work was created from. Find it best-effort:
   ```bash
   git fetch origin
   git reflog show --format='%gs' <current-branch> | tail -1     # "branch: Created from <ref>"; ignore if the ref is HEAD
   # fallback: the remote branch with the fewest commits between it and HEAD
   for b in $(git branch -r --format='%(refname:short)' | grep -v 'HEAD'); do
     git merge-base --is-ancestor "$b" HEAD && echo "$(git rev-list --count "$b"..HEAD) $b"
   done | sort -n | head -3
   ```
   Parent candidates can be `epic/issue<M>` or another `feature/issue<M>` branch (see the note below).
2. **`master`**.

Then decide:

- **Parent is `master`, `develop`, or `epic/*`**: use the parent as the base without asking. These are the
  integration branches a PR is meant to target, so the parent is almost certainly right and asking would only add a
  step. State the choice in one line so the user can object (e.g. "Base branch: epic/issue1 (parent branch)").
- **Parent is anything else (e.g. `feature/*`) or could not be determined**: call `AskUserQuestion` with the
  candidates (parent first, labeled "(Recommended)", then `master`; remove duplicates). A feature branch is not a
  normal merge target, so the user should decide.

Note on `branch-rules.md`: a standalone `feature/issue<N>` targets `master`, a feature inside an epic targets
`epic/issue<M>`. A feature created from another unmerged feature is not covered by the rules. If the parent
candidate is a `feature/*` branch, tell the user that choosing an epic or `master` will include that feature's
commits in the diff, while choosing the feature branch makes a stacked PR.

## Step 3. Validate Commits

Check every commit in `<base>..HEAD` against `commit-rules.md`:
- Subject matches `<Type> #<issue-number> - <subject>` with Type in `Implement | Fix | Refactor | Chore | Docs`, subject in English.
- `Implement` commits have a body explaining what changed and why.
- The issue number matches the branch's issue number.

Report violations as a short list. Do not rewrite history on your own (amend/rebase/force-push change what
others may already have); ask the user whether to fix them or proceed as is.

## Step 4. Verify the Changes

Find the modules touched by the diff and run their tests, so the Verification section reports facts:

```bash
git diff --name-only <base>...HEAD
./gradlew :<module>:test        # for each changed module; use ./gradlew test when changes are build-logic or cross-cutting
```

- Record only commands that were actually run and their real outcome.
- If a command fails, stop and show the failure. Do not open a PR with a hidden failure.
- If a check was skipped (e.g. docs-only change), say so in Verification rather than implying it passed.

## Step 5. Gather Content and Draft

```bash
gh issue view <N> --json title,body,labels,parent
git log --format='%s%n%b' <base>..HEAD
git diff --stat <base>...HEAD
```

**Title**: a short English summary of what the PR does, e.g. ``Add `create-pr` skill``. Do not copy the commit
subject and do not add the `<Type> #<N> -` prefix; the issue is linked by `Closes #<N>` in the body, and a plain
summary is easier to scan in the PR list. Wrap code identifiers (class, function, module, file, property names)
in backticks, since GitHub renders them as inline code in titles and they stand out. Do not use `**bold**`:
titles do not render it and the asterisks would show literally. Start with a verb (`Add`, `Fix`, `Replace`, ...)
and keep it to one line.

**Body** (English, sections separated by `---`, same structure as existing merged PRs):

```markdown
## Overview
Closes #<N>

<One or two sentences: what problem this solves and the approach taken, based on the issue.>

---

## Changes
- <Concrete change, naming files/classes/properties where useful>
- <...>

---

## Verification
- <Command actually run and its result, e.g. Verified all unit tests pass with `./gradlew :feature:home:test`.>
```

Base "Changes" on the real diff, not only the commit subjects, so reviewers get an accurate picture.
If the issue has a parent (epic) issue, mention it in Overview only when it helps the reader.

## Step 6. Labels

```bash
gh label list --limit 100
```

Recommend labels using the issue's labels and the changed paths (e.g. build-logic -> `buildScript`,
Compose UI -> `Compose`, bug fix -> `bug`), then confirm the selection with the user.
Only use labels that exist in the repository; creating new labels is the user's call.

## Step 7. Push and Create the Draft PR

The user chose Draft-first, so no separate approval is needed before pushing; show the final title/body in the
reply afterwards.

```bash
git push -u origin HEAD:<remote-branch>      # <remote-branch> is the name settled in Step 1
gh pr create --draft --base <base> --head <remote-branch> \
  --title "<title>" --body-file <temp-file> --label "<label1>,<label2>"
```

Write the body to a temp file (not inline `--body`) so multi-line Markdown and backticks survive shell quoting.
Assign the PR to the current user (`--assignee @me`) if the linked issue is assigned to them.

## Step 8. Report

Reply in Korean with: PR URL, base <- head branches, labels, what was verified, and any skipped step or commit-rule
violation the user should know about. Remind the user that the PR is a Draft and is theirs to mark Ready.

---

## Common Mistakes

1. **Wrong base branch**: opening against `master` when the work belongs to an epic. Always run Step 2.
2. **Claiming verification that did not happen**: write only what was run; mention skipped checks.
3. **Force-pushing or rewriting commits** to satisfy commit rules without asking.
4. **Using `--body` with long text**: use `--body-file`.
