#!/usr/bin/env bash
# WorktreeCreate hook
#
# Creates a worktree using the same rules as the `.agents/skills/git-worktree-creator` skill.
#   - Location : <repo>/.agents/worktrees/<branch name with '/' replaced by '_'>
#   - Branch   : issue-number names (123, #123, issue123, issue-123) -> feature/issue<number>
#                feature-issue123 / epic-issue123 -> feature|epic/issue<number> as-is
#                other auto-generated names (e.g. bold-oak-a3f2) -> worktree-<name> (temporary branch)
#                names containing '/' are used as-is
#   - Base     : applied only when the branch does not exist yet. Priority:
#                1) the WORKTREE_BASE environment variable
#                2) the session branch, if the session started on an epic/* branch (branch-rules Scenario A)
#                3) master -> main -> HEAD
#   - If the branch already exists, the base is ignored and the branch is checked out as-is
#
# Contract: the last line of stdout must be the absolute worktree path. All other output goes to stderr.
# Difference from the skill: the skill asks the user for prefix/name/base, but the hook is non-interactive, so the rules above replace those questions.
set -euo pipefail

log() { echo "worktree-create: $*" >&2; }

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  raw_name=$(printf '%s' "$input" | jq -r '.name // empty')
else
  raw_name=$(printf '%s' "$input" \
    | sed -n 's/.*"name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
fi
[ -n "$raw_name" ] || { log ".name is empty"; exit 1; }

start_dir="${CLAUDE_PROJECT_DIR:-$PWD}"
# Always resolve against the main repository root, even if the session is inside another worktree.
# (`cd` + `pwd -P` instead of `rev-parse --path-format=absolute`, which requires git >= 2.31)
common_dir=$(cd "$start_dir" && cd "$(git rev-parse --git-common-dir)" && pwd -P)
repo_root=$(dirname "$common_dir")
git_() { git -C "$repo_root" "$@"; }

# --- Resolve branch name ---------------------------------------------------
name="$raw_name"
if [[ "$name" =~ ^(feature|epic)[-_]issue[-_]?([0-9]+)$ ]]; then
  branch="${BASH_REMATCH[1]}/issue${BASH_REMATCH[2]}"
elif [[ "$name" =~ ^#?([0-9]+)$ ]] || [[ "$name" =~ ^issue[-_]?([0-9]+)$ ]]; then
  branch="feature/issue${BASH_REMATCH[1]}"
elif [[ "$name" == */* ]]; then
  branch="$name"
else
  branch="worktree-$name"
fi

git_ check-ref-format --branch "$branch" >/dev/null 2>&1 \
  || { log "invalid branch name: $branch"; exit 1; }

safe_name="${branch//\//_}"
target="$repo_root/.agents/worktrees/$safe_name"

# --- Check target path -----------------------------------------------------
git_ worktree prune >&2 || true   # prune registered worktrees whose directory was deleted

if [ -e "$target" ]; then
  wt_list=$(git_ worktree list --porcelain)
  if grep -Fxq "worktree $target" <<<"$wt_list"; then
    current=$(git -C "$target" symbolic-ref --short -q HEAD || true)
    if [ "$current" = "$branch" ]; then
      log "reusing existing worktree: $target"
      printf '%s\n' "$target"
      exit 0
    fi
    log "existing worktree is on a different branch (${current:-detached}): $target"
    exit 1
  fi
  log "path already exists (not a worktree): $target"
  exit 1
fi
mkdir -p "$repo_root/.agents/worktrees"

# --- Create ----------------------------------------------------------------
if git_ show-ref --verify --quiet "refs/heads/$branch"; then
  git_ worktree add "$target" "$branch" >&2 \
    || { log "the branch may already be checked out in another worktree: $branch"; exit 1; }
else
  base=""
  if [ -n "${WORKTREE_BASE:-}" ]; then
    git_ rev-parse --verify --quiet "${WORKTREE_BASE}^{commit}" >/dev/null \
      || { log "WORKTREE_BASE not found: $WORKTREE_BASE"; exit 1; }
    base="$WORKTREE_BASE"
  else
    session_branch=$(git -C "$start_dir" symbolic-ref --short -q HEAD || true)
    if [[ "$session_branch" == epic/* ]]; then
      base="$session_branch"
    elif git_ show-ref --verify --quiet refs/heads/master; then
      base=master
    elif git_ show-ref --verify --quiet refs/heads/main; then
      base=main
    else
      base=HEAD
    fi
  fi
  log "base=$base branch=$branch"
  git_ worktree add "$target" -b "$branch" "$base" >&2
fi

# --- Copy gitignored files that are required for building -------------------
if [ -f "$repo_root/local.properties" ]; then
  cp "$repo_root/local.properties" "$target/local.properties" \
    || log "failed to copy local.properties (ignored)"
fi

printf '%s\n' "$target"
