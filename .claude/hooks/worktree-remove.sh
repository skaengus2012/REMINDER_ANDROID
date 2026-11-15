#!/usr/bin/env bash
# WorktreeRemove hook
#
# Removes only the worktree directory and always keeps the branch (commits are preserved and can be checked out again later).
# Behaves like the default removal (`git worktree remove --force`), but explicitly guarantees that the branch is left untouched.
# Exit code 0 means the worktree is treated as removed; a non-zero code is treated as a failed removal if the directory still exists.
set -euo pipefail

log() { echo "worktree-remove: $*" >&2; }

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  path=$(printf '%s' "$input" | jq -r '.worktree_path // empty')
else
  path=$(printf '%s' "$input" \
    | sed -n 's/.*"worktree_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
fi
[ -n "$path" ] || { log ".worktree_path is empty"; exit 1; }

# If it is already gone, only prune the registration and report success
if [ ! -e "$path" ]; then
  git -C "${CLAUDE_PROJECT_DIR:-$PWD}" worktree prune >&2 || true
  exit 0
fi

# Only act on paths other than the main repository (safeguard).
common_dir=$(git -C "$path" rev-parse --path-format=absolute --git-common-dir)
repo_root=$(dirname "$common_dir")
[ "$(cd "$path" && pwd -P)" != "$(cd "$repo_root" && pwd -P)" ] \
  || { log "refusing to remove the main repository: $path"; exit 1; }

branch=$(git -C "$path" symbolic-ref --short -q HEAD || true)
git -C "$repo_root" worktree remove --force "$path" >&2
log "worktree removed (branch kept: ${branch:-detached}): $path"
