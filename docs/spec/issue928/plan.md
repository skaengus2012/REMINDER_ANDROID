# 계획: [Task] Review remaining Korean trigger phrases in skill descriptions (#928)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/928
- Requirements: docs/spec/issue928/requirements.md
- Created: 2026-11-17
- Last Updated: 2026-11-17

## Context/Goals
#926 리뷰에서 산출물에 한국어를 피해야 한다는 지적이 있었고, 번역 후에도 `create-pr`, `issue-to-pr` skill의 frontmatter `description`에 한국어 trigger 문구가 남아 있다. 사용자 결정에 따라 이 문구를 영어 예시 발화로 번역하고, 영어/한국어 발화 모두 skill이 trigger되는지 확인한다.

## Requirements
- FR-1: 한국어 trigger 문구를 번역하기로 결정했다 (결정 완료).
- FR-2: 번역 후 한국어 발화(예: "PR 올려줘")와 영어 발화 모두 해당 skill을 trigger하는지 수동으로 검증한다.
- FR-3: 해당 없음 (예외 유지 경로는 채택하지 않음).

범위: `create-pr`, `issue-to-pr`, `create-mockup` 세 skill의 description. `create-mockup`은 요구사항 Open Question 2 결정으로 포함한다.

## Architecture/Design
코드 변경은 없고 skill 메타데이터만 변경한다.

- `.agents/skills/create-pr/SKILL.md` (description 8행)
- `.agents/skills/issue-to-pr/SKILL.md` (description 8-9행)
- `.agents/skills/create-mockup/SKILL.md` (description 7-8행)

결정 사항:
- 한국어 문구는 삭제하지 않고, 같은 의미의 영어 예시 발화로 대체한다. 예: "PR 올려줘" / "PR 만들어줘" 는 "open a PR" / "create a PR" 에 해당하는 영어 표현으로 바꾼다. 정확한 문구는 구현 시 기존 description 문장 흐름에 맞춰 정한다.
- "산출물에서 한국어를 피한다" 원칙을 저장소 문서에 추가하는 작업은 범위 밖이다.
- 예외 문서화는 하지 않는다.

## Tasks
1. `create-pr` description의 한국어 trigger 문구를 동일한 의미의 영어 예시 발화로 대체한다.
   - **Verify**: `grep -nP '[\x{AC00}-\x{D7A3}]' .agents/skills/create-pr/SKILL.md` 의 frontmatter 영역에 결과 없음.
2. `issue-to-pr` description의 한국어 trigger 문구를 동일한 의미의 영어 예시 발화로 대체한다.
   - **Verify**: `grep -nP '[\x{AC00}-\x{D7A3}]' .agents/skills/issue-to-pr/SKILL.md` 의 frontmatter 영역에 결과 없음.
3. `create-mockup` description의 한국어 trigger 문구를 동일한 의미의 영어 예시 발화로 대체한다.
   - **Verify**: `grep -nP '[\x{AC00}-\x{D7A3}]' .agents/skills/create-mockup/SKILL.md` 의 frontmatter 영역에 결과 없음.
4. 세 skill 각각에 대해 변경 후 영어 발화와 원래의 한국어 발화(예: "PR 올려줘", "이슈 886 시작해줘", "시안 만들어줘")를 실제 세션에서 수동으로 입력해, 두 언어 모두 해당 skill이 trigger되는지 확인한다.
   - **Verify**: skill당 영어 1회 이상, 한국어 1회 이상 trigger 확인 결과를 PR 설명에 기록.
5. 변경 범위가 의도한 파일에 한정되었는지 확인한다.
   - **Verify**: `git diff --stat` 에서 위 세 SKILL.md 외 변경 없음 (`docs/spec/issue928/` 제외).

## Verification
testStrategy: unit

- skill 메타데이터만 변경하며 Compose/레이아웃 UI 변경이 없어 UI QA(Maestro)는 하지 않는다. Gradle 모듈 코드 변경이 없어 실행할 단위 테스트도 없다.
- 검증은 Task 1-3의 grep 확인, Task 4의 수동 trigger 확인(영어/한국어 모두), Task 5의 diff 확인으로 한다.
- 수용 기준 대응: 방침 결정(FR-1) = 결정 완료, 번역 후 trigger 확인(FR-2) = Task 1-4.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-17 | Initial draft (방침 미결정, 경로 A/B 조건부 Task) | requirements.md |
| 2026-11-17 | 번역 경로로 확정: 경로 B와 결정 게이트 Task 제거 후 재번호, `create-mockup` 범위 포함, 영어 예시 발화로 대체, 영어/한국어 수동 trigger 검증으로 변경, 원칙 문서화는 범위 밖 | 사용자 결정 (Open Questions 1-6) |
