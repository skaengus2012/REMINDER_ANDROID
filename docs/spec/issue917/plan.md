# 계획: [Task] Rename git-worktree-creator skill to create-worktree (#917)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/917
- 요구사항: docs/spec/issue917/requirements.md
- 생성일: 2026-11-15
- 최종 수정일: 2026-11-15

## 배경 및 목표
`.agents/skills/` 하위에서 무언가를 생성하는 스킬은 `create-` 접두사를 사용하지만(`create-pr`, `create-mockup`, `create-jacoco-report-and-show`), worktree 스킬만 `git-worktree-creator`로 규칙을 벗어나 있다. 이름을 `create-worktree`로 바꿔 접두사 기반 탐색성과 일관성을 확보한다.

## 요구사항
- FR-1: `git-worktree-creator` 스킬을 `create-worktree`로 이름을 변경한다.
- FR-2: `SKILL.md` frontmatter의 `name`을 새 디렉터리 이름(`create-worktree`)과 일치시킨다.
- 사용자 결정(2026-11-15)
  - `SKILL.md` 8행 제목을 `# Create Worktree Skill`로 함께 변경한다.
  - 과거 이슈 문서(`docs/spec/issue886`, `issue887`)의 이전 이름 언급은 당시 기록이므로 수정하지 않는다.

## 설계
- 파일 이력 보존을 위해 `git mv`로 디렉터리 이름을 변경한다.
- 변경 범위는 `.agents/skills/create-worktree/SKILL.md`의 `name`과 제목 두 곳으로 한정하며, 스킬의 동작과 `description`은 바꾸지 않는다.
- `.claude/skills`는 `../.agents/skills`로의 심볼릭 링크이므로 별도 수정이 필요 없다.
- `AGENTS.md`, `issue-to-pr`, `create-pr`, `plan-to-pr.js`, `~/.claude`에는 이전 이름 참조가 없음을 확인했다(변경 불필요).

## 작업
1. `git mv .agents/skills/git-worktree-creator .agents/skills/create-worktree`로 디렉터리 이름을 변경한다.
   - **검증**: `ls .agents/skills`에 `create-worktree`가 보이고 `git-worktree-creator`는 보이지 않는다.
2. `.agents/skills/create-worktree/SKILL.md`의 frontmatter `name`을 `create-worktree`로, 8행 제목을 `# Create Worktree Skill`로 수정한다.
   - **검증**: `head -8 .agents/skills/create-worktree/SKILL.md`에서 `name: create-worktree`와 `# Create Worktree Skill`이 확인된다.
3. 저장소 전체에서 이전 이름 잔존 여부와 심볼릭 링크 동작을 확인한다.
   - **검증**: `grep -rn "git-worktree-creator" --exclude-dir=.git --exclude-dir=build .` 결과가 `docs/spec/issue886`, `docs/spec/issue887`, `docs/spec/issue917`의 기록성 언급만 남는다. `ls .claude/skills`에 `create-worktree`가 보인다.

## 검증
- 인수 조건 1: `.agents/skills/create-worktree/`가 존재하고 `.agents/skills/git-worktree-creator/`는 존재하지 않는다. (작업 1)
- 인수 조건 2: `SKILL.md` frontmatter `name`이 `create-worktree`이다. (작업 2)
- 이전 이름 참조가 동작에 영향을 주는 위치에 남아 있지 않다. (작업 3)
- 코드 변경이 없는 문서/스킬 변경이므로 Gradle 빌드와 테스트는 대상이 아니다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-15 | 최초 작성 | requirements.md, 사용자 결정(제목 변경 포함, 과거 문서 유지) |
