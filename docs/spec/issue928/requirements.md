# 요구사항 분석: [Task] Review remaining Korean trigger phrases in skill descriptions (#928)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/928
- Parent issue: None
- Labels: AX
- Created: 2026-11-17
- Last Updated: 2026-11-17
- Analysis Baseline: 2026-11-17T11:22:36Z

## Background and Problem
#926 리뷰에서 산출물에는 한국어를 피해야 한다는 지적이 있었다. 해당 PR에서 추가한 한국어 예시를 번역한 뒤에도, 두 skill의 frontmatter `description`에 있는 trigger 문구에 한국어가 남아 있다.

- `.agents/skills/create-pr/SKILL.md:8`
- `.agents/skills/issue-to-pr/SKILL.md:8-9`

이 문구는 skill 자동 호출 매칭에 사용되므로, 번역하면 "PR 올려줘" 같은 한국어 발화에 대한 trigger 정확도가 떨어질 수 있다. #926에서는 현 상태를 유지하기로 결정했다.

## Goals
skill `description`에 남은 한국어 trigger 문구를 "산출물에서 한국어를 피한다"는 원칙에 맞출지 결정하고, 그 결정을 반영한다.

## Scope
### In Scope
- `create-pr`, `issue-to-pr` skill의 `description` 내 한국어 trigger 문구에 대한 처리 방침 결정
- 번역하는 경우: 한국어 발화로 두 skill이 여전히 안정적으로 trigger되는지 검증
- 번역하지 않는 경우: trigger 문구에 대한 명시적 예외를 문서화
### Out of Scope
- N/A (이슈에 명시된 제외 범위 없음)

## Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | 남아 있는 한국어 trigger 문구가 "산출물에서 한국어를 피한다" 원칙을 따라야 하는지 결정한다. | Body (Needs) |
| FR-2 | 번역하기로 결정한 경우, 한국어 발화(예: "PR 올려줘")가 두 skill을 안정적으로 trigger하는지 검증한다. | Body (Needs) |
| FR-3 | 번역하지 않기로 결정한 경우, trigger 문구에 대한 명시적 예외를 문서화한다. | Body (Needs) |

## Non-Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| - | N/A (이슈에 명시된 비기능 요구사항 없음) | - |

## Acceptance Criteria
- [ ] 한국어 trigger 문구의 처리 방침(번역 또는 예외 유지)이 결정되어 있다 (FR-1).
- [ ] 번역한 경우, 한국어 발화로 `create-pr`와 `issue-to-pr`가 안정적으로 trigger됨이 확인되었다 (FR-2).
- [ ] 번역하지 않은 경우, trigger 문구 예외가 문서화되어 있다 (FR-3).

## Impact
코드로 확인한 범위:
- `.agents/skills/create-pr/SKILL.md` 8행: "PR 올려줘" / "PR 만들어줘"
- `.agents/skills/issue-to-pr/SKILL.md` 8-9행: "이슈 처음부터 PR까지 진행해줘" / "이슈 886 시작해줘"
- `.agents/skills/create-mockup/SKILL.md` 7-8행에도 동일한 형태의 한국어 trigger 문구("시안 만들어줘" / "목업 만들어줘" / "디자인 해줘")가 있으나, 이슈 본문에는 언급되지 않았다.
- 예외를 문서화하는 경우 후보 위치: `AGENTS.md`, `docs/guidelines/` (이슈에 지정되지 않음)

## Open Questions
1. FR-1의 결정 자체: 한국어 trigger 문구를 번역할 것인가, 예외로 유지할 것인가? (이슈는 결정을 요구할 뿐 방향을 정하지 않았다.)
2. `create-mockup` skill(7-8행)의 한국어 trigger 문구도 범위에 포함하는가? 이슈는 두 skill만 명시한다.
3. 번역 시, 한국어 문구를 영어로 대체할 것인가, 삭제할 것인가? 영어 대체 시 구체적 표현은 무엇인가?
4. FR-2 "안정적으로 trigger"의 판정 기준(발화 수, 성공 기준)과 검증 방법(실제 세션 수동 확인, 평가 스크립트 등)이 정해지지 않았다.
5. 예외로 유지할 경우(FR-3), "trigger 문구는 한국어 허용"을 문서화할 위치와 문구는? (`AGENTS.md`, `docs/guidelines/`, 각 SKILL.md 주석 등)
6. "산출물에서 한국어를 피한다" 원칙은 현재 어디에 명시되어 있는가? #926 리뷰 코멘트에만 있고 저장소 문서에는 없다면, 이 원칙 자체를 문서화하는 작업이 이 이슈에 포함되는가?
7. 변경 결과를 PR로 낼 때 base 브랜치(master)와 Draft PR 생성 여부는 어떻게 하는가?

## Conflicts and Notes
- 이슈에 댓글과 부모 이슈가 없어 충돌하는 출처는 없다.
- 이 이슈는 구현 방향 결정 자체가 요구사항이므로, FR-2와 FR-3은 FR-1의 결정 결과에 따라 택일로 적용된다.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-17 | Initial draft | Body |
