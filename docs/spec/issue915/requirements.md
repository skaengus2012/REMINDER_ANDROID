# Requirements Analysis: [Task] Add WorktreeCreate/WorktreeRemove hooks for agent worktrees (#915)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/915
- Parent issue: None
- Labels: AX, buildScript
- Created: 2026-11-15
- Last Updated: 2026-11-15
- Analysis Baseline: 2026-11-15T00:26:37Z

## Background and Problem
Agent sessions create git worktrees through the app/CLI default behavior, which places them under `.claude/worktrees/` and does not follow this project's conventions. The conventions already exist in the `git-worktree-creator` skill and `docs/guidelines/branch-rules.md`, but they are only applied when the skill is invoked manually and interactively.

As a result:
- Worktree location and branch naming differ depending on how a worktree was created.
- Gitignored files required for a build (`local.properties`) are missing in new worktrees.
- Removal behavior on session end/delete is not explicit, so it is unclear whether commits and branches are preserved.

## Goals
Make worktree creation and removal follow the project's conventions automatically, regardless of how the worktree is created, by providing project-shared `WorktreeCreate` / `WorktreeRemove` hooks.

## Scope
### In Scope
- A `WorktreeCreate` hook (`.claude/hooks/worktree-create.sh`) that reproduces the `git-worktree-creator` skill non-interactively.
- A `WorktreeRemove` hook (`.claude/hooks/worktree-remove.sh`) that removes only the worktree directory and keeps the branch.
- Registering both hooks in the project-shared `.claude/settings.json`.
- Verifying in the Claude desktop app how the hooks are triggered.

### Out of Scope
- Changing the behavior of the `git-worktree-creator` skill itself.
- Changing `docs/guidelines/branch-rules.md`.

## Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | Worktrees are created at `.agents/worktrees/<branch with "/" replaced by "_">`, always under the main repository root, even when the session starts inside another worktree. | Body |
| FR-2 | Issue-number names (`123`, `#123`, `issue123`, `issue-123`) become `feature/issue<number>`; `feature-issue<N>` / `epic-issue<N>` map to `feature|epic/issue<N>`; names containing `/` are used as-is; other auto-generated names become a temporary `worktree-<name>` branch. | Body |
| FR-3 | When the branch does not exist yet, the base branch is chosen in this order: `WORKTREE_BASE` > session branch if `epic/*` > `master` > `main` > `HEAD`. An existing branch is checked out as-is and the base is ignored. | Body |
| FR-4 | An existing worktree at the target path is reused only if it is on the expected branch; otherwise creation fails. | Body |
| FR-5 | `local.properties` is copied into the new worktree; a copy failure is non-fatal. | Body |
| FR-6 | The `WorktreeRemove` hook removes only the worktree directory and always keeps the branch; it refuses to remove the main repository and treats an already-missing path as success. | Body |
| FR-7 | Both hooks are registered in the project-shared `.claude/settings.json`. | Body |

## Non-Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| NFR-1 | The hooks follow the Claude Code hook contract (the create hook prints only the absolute worktree path as the last stdout line; all other output goes to stderr). | Body |

## Acceptance Criteria
- [x] `.claude/hooks/worktree-create.sh` implements FR-1 to FR-5.
- [x] `.claude/hooks/worktree-remove.sh` implements FR-6.
- [x] Both hooks are registered in `.claude/settings.json`.
- [ ] Verified in the Claude desktop app that the Worktree toggle goes through the `WorktreeCreate` hook.
- [ ] Verified in the Claude desktop app that the `WorktreeRemove` hook runs when a session is closed, archived, or deleted.
- [x] `.claude/worktrees/` is ignored in `.gitignore` (optional follow-up from the issue).
- [x] Changes are committed and pushed on `feature/issue915`.

## Impact
- `.claude/hooks/worktree-create.sh` (new)
- `.claude/hooks/worktree-remove.sh` (new)
- `.claude/settings.json` (new)
- `docs/guidelines/branch-rules.md` (reference rules; unchanged)
- `.gitignore` (`.agents/worktrees/` already ignored; `.claude/worktrees/` added)
- `docs/spec/issue915/plan.md` (new)

## Open Questions
- Does the desktop app's Worktree toggle go through the `WorktreeCreate` hook? The Claude Code docs do not list it explicitly; if it does not, the default `.claude/worktrees/` location is used.
- Does the `WorktreeRemove` hook run when a session is closed, archived, or deleted in the desktop app? Docs confirm it for interactive CLI exit, unnamed-session auto-removal, background session deletion, and `ExitWorktree`.
- Should the main checkout be switched back to the parent branch when a worktree is removed? A worktree does not change the main checkout's branch, so this was interpreted as the session returning to its original directory, which the app/CLI handles. It is not implemented in the hook.
- Is the temporary `worktree-<name>` branch for non-issue names acceptable under `branch-rules.md`, which only lists `master`, `epic/*`, and `feature/*`?
- Should the base fall back to `origin/master` instead of the local `master`, to avoid branching from a stale local branch? The skill uses the local branch.
- Should a variant that force-removes worktrees at session end regardless of changes (e.g. via `SessionEnd`) be considered? The `WorktreeRemove` hook only runs after the app/CLI decides to remove.

## Conflicts and Notes
- No comments exist on the issue, so there are no conflicts between the body and comments.
- The hooks replace the default git behavior; `.worktreeinclude` is not processed once `WorktreeCreate` is registered, so file copying is done inside the script.
- `jq` is used when available; a `sed` fallback is used otherwise.
- Implementation was cross-reviewed with a separate model; its high/medium findings (non-issue branch naming, epic base selection, reuse validation, `worktree prune`, SIGPIPE with `pipefail`, jq fallback, non-fatal copy failure) are already reflected.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-15 | Initial draft | Body |
| 2026-11-15 | Resolved the `.gitignore` open question by implementing it; marked its acceptance criterion as done | User request (plan Task 4) |
| 2026-11-15 | Dropped the optional `SKILL.md` follow-up: the hooks live in `.claude/` and only Claude Code triggers them, so the skill needs no description of them | User request |
| 2026-11-15 | Replaced `rev-parse --path-format=absolute` (git >= 2.31) with `cd` + `pwd -P` so the hooks also work on older git (e.g. 2.23) | Cross-review |
