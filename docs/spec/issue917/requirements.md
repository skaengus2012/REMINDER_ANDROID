# 요구사항 분석: [Task] Rename git-worktree-creator skill to create-worktree (#917)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/917
- 상위 이슈: 없음
- 라벨: AX
- 생성일: 2026-11-15
- 최종 수정일: 2026-11-15
- 분석 기준점(Analysis Baseline): 2026-11-15T02:29:40Z (댓글 없음, 이슈 `updatedAt` 기준)

## 배경 및 문제
`.agents/skills/` 하위 스킬 이름의 접두사가 일관되지 않다. 무언가를 생성하는 스킬은 `create-` 접두사를 사용하지만(`create-pr`, `create-mockup`, `create-jacoco-report-and-show`), worktree 스킬만 `git-worktree-creator`라는 이름이라 규칙에서 벗어나 있으며, 접두사로 스킬을 찾기 어렵다.

## 목표
스킬 이름 규칙(`create-` 접두사)을 worktree 스킬에도 적용해 일관성과 접두사 기반 탐색성을 확보한다.

## 범위
### 포함
- `git-worktree-creator` 스킬을 `create-worktree`로 이름 변경
- `SKILL.md` frontmatter의 `name` 필드를 새 디렉터리 이름과 일치하도록 수정

- `SKILL.md` 8행 제목(`# Git Worktree Creator Skill`)을 `# Create Worktree Skill`로 변경 (사용자 결정, 2026-11-15)

### 제외
- 이슈에 명시되지 않은 스킬 내용(동작, 설명)의 변경
- 과거 이슈 문서(`docs/spec/issue886`, `issue887`)에 남은 이전 이름 언급의 갱신 (사용자 결정: 당시 기록으로 유지)

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `git-worktree-creator` 스킬을 `create-worktree`로 이름을 변경한다. | 본문 (Needs) |
| FR-2 | `SKILL.md` frontmatter의 `name` 필드를 새 디렉터리 이름(`create-worktree`)과 일치시킨다. | 본문 (Needs) |

## 비기능 요구사항
N/A

## 인수 조건
- [ ] `.agents/skills/create-worktree/` 디렉터리가 존재하고 `.agents/skills/git-worktree-creator/`는 존재하지 않는다.
- [ ] `.agents/skills/create-worktree/SKILL.md`의 frontmatter `name`이 `create-worktree`이다.
- [ ] `.agents/skills/create-worktree/SKILL.md`의 제목이 `# Create Worktree Skill`이다.

## 영향 범위
코드베이스에서 확인한 `git-worktree-creator` 참조 위치는 다음과 같다.

- `.agents/skills/git-worktree-creator/SKILL.md:2` — frontmatter `name` (변경 대상)
- `.agents/skills/git-worktree-creator/SKILL.md:8` — 제목 `# Git Worktree Creator Skill` (변경 대상)
- `.claude/skills`는 `../.agents/skills`로의 심볼릭 링크이므로 디렉터리 이름 변경만으로 따라가며, 이전 이름을 하드코딩한 설정은 없다.
- `issue-to-pr` 스킬, `create-pr` 스킬, `.agents/workflows/plan-to-pr.js`, `~/.claude`에는 이전 이름 참조가 없다.
- `docs/spec/issue887/plan.md:25`, `docs/spec/issue887/requirements.md:46`, `docs/spec/issue886/requirements.md:70` — 과거 이슈 문서 내 스킬 이름 언급
- `AGENTS.md`의 가이드라인 인덱스에는 `git-worktree-creator`가 직접 언급되어 있지 않다(`.agents/skills/` 일반 항목으로만 안내).

## 미해결 질문
N/A

## 충돌 및 참고
본문과 충돌하는 댓글이 없다(댓글 0건). 상위 이슈도 없어 추가 배경 정보는 없다.

이슈 본문에 명시되지 않은 두 가지는 사용자 결정으로 정리했다(2026-11-15). 제목 변경은 범위에 포함하고, 과거 이슈 문서의 이전 이름 언급은 갱신하지 않는다. `description`에는 이전 이름이 없어 변경 대상이 아니다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-15 | 최초 작성 | 본문 |
| 2026-11-15 | 미해결 질문 3건 해소: 제목 변경 포함, 과거 문서 유지, 심볼릭 링크/기타 참조 없음 확인. 범위·인수 조건·영향 범위 갱신 | 사용자 결정, 코드베이스 확인 |
