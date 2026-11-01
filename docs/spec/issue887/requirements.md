# 요구사항 분석: [Task] Add .claude/skills symlink to .agents/skills for Claude Code (#887)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/887
- 부모 이슈: 없음
- 라벨: AX
- 생성일: 2026-11-01
- 최종 수정일: 2026-11-02
- 분석 기준점: 2026-11-01T15:51:03Z

## 배경 및 문제
- 프로젝트 스킬은 `.agents/skills/`에 있으나, Claude Code는 프로젝트 스킬을 `.claude/skills/` 아래에서만 탐색한다.
- 링크가 없으면 `create-pr`, `create-mockup` 같은 스킬을 Claude Code에서 사용할 수 없다.
- 이슈에 댓글은 없다.

## 목표
- Claude Code에서 `.agents/skills/`의 프로젝트 스킬을 사용할 수 있게 한다.
- `.agents/skills`를 프로젝트 스킬의 단일 원본(single source of truth)으로 유지한다.

## 범위
### 포함
- `.claude/skills` 심볼릭 링크 추가 (대상: `../.agents/skills`)
- 심볼릭 링크 커밋

### 제외
- 스킬 파일의 복제 또는 이동
- 스킬 내용 자체의 변경

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `.claude/skills`를 `../.agents/skills`를 가리키는 심볼릭 링크로 추가한다. | 본문 |
| FR-2 | `.agents/skills`를 프로젝트 스킬의 단일 원본으로 유지하며, 파일을 복제하지 않는다. | 본문 |
| FR-3 | 심볼릭 링크를 커밋하여 모든 clone에서 사용 가능하게 한다. | 본문 |

## 비기능 요구사항
N/A

## 인수 조건
- [ ] `.claude/skills`가 `../.agents/skills`를 가리키는 심볼릭 링크이다.
- [ ] `.agents/skills` 외에 스킬 파일의 복사본이 존재하지 않는다.
- [ ] 심볼릭 링크가 git에 커밋되어 있다 (clone 시 포함).
- [ ] Claude Code에서 `create-pr`, `create-mockup` 등 `.agents/skills`의 스킬이 인식된다.

## 영향 범위
- `.claude/` 디렉터리: 현재 존재하지 않음 (신규 생성 필요)
- `.agents/skills/`: `create-jacoco-report-and-show`, `create-mockup`, `create-pr`, `git-worktree-creator`
- `.gitignore`에 `claude` 관련 항목은 없음 (확인 결과)
- `AGENTS.md`의 가이드 인덱스가 `.agents/skills/`를 참조함

## 미결 사항
- 링크 외에 `AGENTS.md` 등 문서에 이 구성(단일 원본 및 링크)을 명시할지 여부 (이슈에 언급 없음)
- Windows 환경에서의 심볼릭 링크 지원 여부 (이슈에 언급 없음)

## 충돌 및 참고
- 댓글이 없어 본문과 댓글 간 충돌은 없다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-02 | 최초 작성 | 본문 |
