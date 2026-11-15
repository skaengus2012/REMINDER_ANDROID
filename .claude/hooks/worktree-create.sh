#!/usr/bin/env bash
# WorktreeCreate hook
#
# `.agents/skills/git-worktree-creator` 스킬과 동일한 규칙으로 워크트리를 만든다.
#   - 위치   : <repo>/.agents/worktrees/<branch 의 '/' 를 '_' 로 치환한 이름>
#   - 브랜치 : 이슈 번호 이름(123, #123, issue123, issue-123) → feature/issue<번호>
#              feature-issue123 / epic-issue123 형태는 그대로 feature|epic/issue<번호>
#              그 외 자동 생성 이름(예: bold-oak-a3f2) → worktree-<name> (임시 브랜치)
#              '/' 가 포함된 이름은 그대로 사용
#   - 기준   : 브랜치가 없을 때만 적용. 우선순위
#              1) 환경변수 WORKTREE_BASE
#              2) 세션이 epic/* 브랜치에서 시작됐으면 그 브랜치 (branch-rules Scenario A)
#              3) master → main → HEAD
#   - 브랜치가 이미 있으면 base 를 무시하고 그대로 체크아웃
#
# 계약: stdout 의 마지막 줄은 워크트리 절대경로여야 한다. 그 외 출력은 모두 stderr 로 보낸다.
# 스킬과의 차이: 스킬은 prefix/이름/base 를 사용자에게 묻지만, 훅은 비대화형이라 위 규칙으로 대체한다.
set -euo pipefail

log() { echo "worktree-create: $*" >&2; }

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  raw_name=$(printf '%s' "$input" | jq -r '.name // empty')
else
  raw_name=$(printf '%s' "$input" \
    | sed -n 's/.*"name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
fi
[ -n "$raw_name" ] || { log ".name 이 비어 있습니다"; exit 1; }

start_dir="${CLAUDE_PROJECT_DIR:-$PWD}"
# 세션이 다른 워크트리 안에 있어도 항상 메인 저장소 루트를 기준으로 한다.
common_dir=$(git -C "$start_dir" rev-parse --path-format=absolute --git-common-dir)
repo_root=$(dirname "$common_dir")
git_() { git -C "$repo_root" "$@"; }

# --- 브랜치 이름 결정 ------------------------------------------------------
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
  || { log "유효하지 않은 브랜치 이름: $branch"; exit 1; }

safe_name="${branch//\//_}"
target="$repo_root/.agents/worktrees/$safe_name"

# --- 대상 경로 확인 --------------------------------------------------------
git_ worktree prune >&2 || true   # 디렉터리만 지워진 등록 워크트리 정리

if [ -e "$target" ]; then
  wt_list=$(git_ worktree list --porcelain)
  if grep -Fxq "worktree $target" <<<"$wt_list"; then
    current=$(git -C "$target" symbolic-ref --short -q HEAD || true)
    if [ "$current" = "$branch" ]; then
      log "기존 워크트리를 재사용합니다: $target"
      printf '%s\n' "$target"
      exit 0
    fi
    log "기존 워크트리가 다른 브랜치(${current:-detached})에 있습니다: $target"
    exit 1
  fi
  log "경로가 이미 존재합니다(워크트리 아님): $target"
  exit 1
fi
mkdir -p "$repo_root/.agents/worktrees"

# --- 생성 ------------------------------------------------------------------
if git_ show-ref --verify --quiet "refs/heads/$branch"; then
  git_ worktree add "$target" "$branch" >&2 \
    || { log "브랜치가 이미 다른 워크트리에 체크아웃되어 있을 수 있습니다: $branch"; exit 1; }
else
  base=""
  if [ -n "${WORKTREE_BASE:-}" ]; then
    git_ rev-parse --verify --quiet "${WORKTREE_BASE}^{commit}" >/dev/null \
      || { log "WORKTREE_BASE 를 찾을 수 없습니다: $WORKTREE_BASE"; exit 1; }
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

# --- gitignore 대상이지만 빌드에 필요한 파일 복사 ---------------------------
if [ -f "$repo_root/local.properties" ]; then
  cp "$repo_root/local.properties" "$target/local.properties" \
    || log "local.properties 복사 실패(무시)"
fi

printf '%s\n' "$target"
