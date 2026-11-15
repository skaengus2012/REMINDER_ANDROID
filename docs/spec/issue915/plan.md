# Plan: [Task] Add WorktreeCreate/WorktreeRemove hooks for agent worktrees (#915)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/915
- Requirements: docs/spec/issue915/requirements.md
- Created: 2026-11-15
- Last Updated: 2026-11-15

## Context/Goals
Agent sessions create worktrees with the app/CLI default (`.claude/worktrees/`), ignoring the conventions defined by the `git-worktree-creator` skill and `docs/guidelines/branch-rules.md`. Goal: provide project-shared `WorktreeCreate` / `WorktreeRemove` hooks so worktree location, branch naming, base selection, build-required file copying, and removal always follow the project's conventions without an interactive skill run.

## Requirements
- FR-1: Location `.agents/worktrees/<branch with "/" → "_">` under the main repository root.
- FR-2: Branch naming (issue-number names → `feature/issue<N>`, `feature|epic-issue<N>` → `feature|epic/issue<N>`, names with `/` as-is, other names → `worktree-<name>`).
- FR-3: Base selection `WORKTREE_BASE` > `epic/*` session branch > `master` > `main` > `HEAD`; existing branch ignores the base.
- FR-4: Reuse an existing worktree only on the expected branch.
- FR-5: Copy `local.properties` (non-fatal on failure).
- FR-6: Removal deletes only the worktree directory and keeps the branch; refuses the main repository; missing path is success.
- FR-7: Register both hooks in `.claude/settings.json`.
- NFR-1: Follow the hook contract (last stdout line is the absolute path; everything else to stderr).
- Optional follow-ups from the issue: ignore `.claude/worktrees/` in `.gitignore`; document the hook/skill relationship in `SKILL.md`.

## Architecture/Design
### Hook vs. skill
The skill is interactive (asks prefix, name, base). A `WorktreeCreate` hook receives only `name` on stdin, so the questions are replaced by deterministic rules (FR-2, FR-3). The hook **replaces** the default git behavior and `.worktreeinclude` is not processed, so file copying lives in the script (FR-5).

### `worktree-create.sh`
- Parse `.name` with `jq`, falling back to `sed` when `jq` is missing.
- Resolve the main repository root from `git rev-parse --git-common-dir` made absolute with `cd` + `pwd -P` (`--path-format=absolute` needs git >= 2.31 and breaks older git such as 2.23), so a session started inside another worktree still creates worktrees under the main root.
- Map the name to a branch (FR-2), validate with `git check-ref-format --branch`, derive `safe_name` and the target path (FR-1).
- `git worktree prune` first, then handle an existing target: reuse only if it is registered and on the expected branch (FR-4); otherwise fail.
- Existing branch → `git worktree add <path> <branch>`; new branch → `git worktree add <path> -b <branch> <base>` with the base chosen as in FR-3 (`WORKTREE_BASE` is validated).
- Copy `local.properties` (FR-5), then print only the absolute path on stdout.

### `worktree-remove.sh`
- Parse `.worktree_path`; if it no longer exists, `git worktree prune` and exit 0.
- Refuse the main repository path; otherwise `git worktree remove --force <path>`. The branch is never touched (FR-6).
- `--force` mirrors the default removal; the app/CLI already asks the user before removing a worktree that has changes.

### Registration
`.claude/settings.json` registers both hooks with `bash "$CLAUDE_PROJECT_DIR"/.claude/hooks/<script>`; the `bash` prefix keeps the hooks working even if the execute bit is lost.

### Decisions (defaults chosen without explicit user confirmation)
- Non-issue names become `worktree-<name>` temporary branches. Rejecting them would block sessions whose names are auto-generated, and prefixing them with `feature/` would violate `branch-rules.md`. This is an open question for the user.
- The base uses the local `master`, matching the skill, instead of `origin/master`.
- "Return to the parent branch after removal" is interpreted as the session returning to its original directory (handled by the app/CLI); the hook does not switch the main checkout's branch.

## Tasks
1. Add `.claude/hooks/worktree-create.sh` implementing FR-1 to FR-5. *(Done)*
   - **Verify**: `bash -n .claude/hooks/worktree-create.sh`, and manual runs for each name form (numeric, `epic-issue<N>`, auto-generated), reuse, mismatched branch, `WORKTREE_BASE` valid/invalid, and no-`jq`.
2. Add `.claude/hooks/worktree-remove.sh` implementing FR-6. *(Done)*
   - **Verify**: create a worktree with an untracked file, run the hook; the directory is gone, the branch remains; a missing path exits 0; the main repository path exits 1.
3. Register both hooks in `.claude/settings.json` (FR-7). *(Done)*
   - **Verify**: `jq . .claude/settings.json`
4. Ignore `.claude/worktrees/` in `.gitignore` so teammates do not see the default-location worktrees as untracked.
   - **Verify**: `git check-ignore -v .claude/worktrees/x`
5. Document the relationship between the hooks and the skill in `.agents/skills/git-worktree-creator/SKILL.md`, especially the differences in prefix/name/base selection.
   - **Verify**: the new section exists in `SKILL.md` and matches the rules in FR-2/FR-3.
6. Sync `requirements.md` (acceptance criteria, open questions, change history) with the work done in Tasks 4 and 5.
   - **Verify**: resolved open questions are removed and recorded in `Change History`.
7. Verify in the Claude desktop app that the Worktree toggle goes through `WorktreeCreate` and that `WorktreeRemove` runs on session close/archive/delete. *(Manual; pending)*
   - **Verify**: start a worktree session from the desktop app and check the path/branch; close or delete it and confirm the directory is removed while the branch remains.

## Verification
testStrategy: manual

- No production Android code changes, so Gradle unit tests and Maestro UI QA are not applicable.
- Hook scripts are verified with `bash -n` and the manual scenarios listed in Tasks 1 and 2, each of which cleans up its temporary worktrees and branches.
- The hooks were cross-reviewed by a separate model; its high/medium findings are reflected in the scripts.
- Task 7 can only be confirmed by using the desktop app; until then FR-7 is verified only at the configuration level.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-15 | Initial draft | requirements.md / user request |
