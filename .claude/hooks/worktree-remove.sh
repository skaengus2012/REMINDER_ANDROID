#!/usr/bin/env bash
# WorktreeRemove hook
#
# 워크트리 디렉터리만 제거하고 브랜치는 항상 남긴다(커밋 보존, 나중에 다시 체크아웃 가능).
# 기본 제거(`git worktree remove --force`)와 같지만 브랜치를 건드리지 않는다는 점을 명시적으로 보장한다.
# 종료 코드 0 이면 제거된 것으로 간주되며, 0 이 아니면 디렉터리가 남아 있을 때 제거가 실패한 것으로 처리된다.
set -euo pipefail

log() { echo "worktree-remove: $*" >&2; }

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  path=$(printf '%s' "$input" | jq -r '.worktree_path // empty')
else
  path=$(printf '%s' "$input" \
    | sed -n 's/.*"worktree_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
fi
[ -n "$path" ] || { log ".worktree_path 가 비어 있습니다"; exit 1; }

# 이미 없으면 등록 정보만 정리하고 성공 처리
if [ ! -e "$path" ]; then
  git -C "${CLAUDE_PROJECT_DIR:-$PWD}" worktree prune >&2 || true
  exit 0
fi

# 메인 저장소가 아닌 경로만 대상으로 한다(안전장치).
common_dir=$(git -C "$path" rev-parse --path-format=absolute --git-common-dir)
repo_root=$(dirname "$common_dir")
[ "$(cd "$path" && pwd -P)" != "$(cd "$repo_root" && pwd -P)" ] \
  || { log "메인 저장소는 제거하지 않습니다: $path"; exit 1; }

branch=$(git -C "$path" symbolic-ref --short -q HEAD || true)
git -C "$repo_root" worktree remove --force "$path" >&2
log "워크트리 제거 완료(브랜치 유지: ${branch:-detached}): $path"
