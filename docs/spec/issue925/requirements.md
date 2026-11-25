# 요구사항 분석: [Bug] issue-to-pr skill/workflow errors on every run (#925)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/925
- Parent issue: None
- Labels: AX
- Created: 2026-11-16
- Last Updated: 2026-11-17
- Analysis Baseline: 2026-11-16T16:53:12Z

## Background and Problem
`issue-to-pr` skill과 `plan-to-pr` workflow를 실행할 때마다 오류가 반복적으로 발생한다.

확인된 예시: Step 2 ("Ask Before Starting")의 시작 지점 질문은 기존 spec 문서에 해당하는 선택지만 제시한다. `docs/spec/issue<N>/` 파일이 없으면 선택지가 "From the beginning" 하나만 남는다. 선택지가 1개인 질문을 `AskUserQuestion`에 전달하면 `InputValidationError`(질문당 최소 2개 선택지)로 실패하고, 호출 전체가 거부되어 사용자는 어떤 질문도 보지 못한다.

그 외 workflow 실행 중 발생한 오류는 아직 정리되지 않았다.

## Goals
`issue-to-pr` skill과 `plan-to-pr` workflow가 반복 오류 없이 실행되도록 한다.

## Scope
### In Scope
- `issue-to-pr` skill에 "선택지가 2개 미만인 질문은 묻지 않는다" 규칙 추가
- skill의 나머지 단계(Gate 1/2, Step 7 준비)와 `.agents/workflows/plan-to-pr.js`의 제약 위반/반복 실패 검토 및 수정
- 과거 실행에서 관찰된 오류를 수집해 이슈에 체크리스트로 기록
### Out of Scope
- N/A (이슈에 명시된 제외 범위 없음)

## Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | `issue-to-pr` skill에 규칙을 추가한다: 질문의 선택지가 2개 미만이면 해당 질문을 하지 않고, 선택된 경로를 명시한 뒤 계속 진행하며, 나머지 질문만 묻는다. | Body (Needs) |
| FR-2 | skill의 나머지 단계(Gate 1/2, Step 7 준비)와 `.agents/workflows/plan-to-pr.js`를 검토해 다른 제약 위반이나 반복 실패를 찾아 수정한다. | Body (Needs) |
| FR-3 | 과거 실행에서 관찰된 오류를 수집해 이 이슈에 체크리스트로 기록한다. | Body (Needs) |

## Non-Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| - | N/A (이슈에 명시된 비기능 요구사항 없음) | - |

## Acceptance Criteria
- [ ] 선택지가 1개뿐인 질문이 `AskUserQuestion`에 전달되지 않고, 선택된 경로가 명시되며 나머지 질문만 묻는다 (FR-1).
- [ ] Gate 1/2, Step 7 및 `plan-to-pr.js`에서 발견된 제약 위반/반복 실패가 수정되었다 (FR-2).
- [ ] 과거 실행에서 관찰된 오류가 이슈에 체크리스트로 기록되었다 (FR-3).

## Impact
코드로 확인한 범위:
- `.agents/skills/issue-to-pr/SKILL.md`: Step 2는 단일 `AskUserQuestion` 호출로 4개 질문(시작 지점, 최대 리뷰 라운드, Draft PR 생성 여부, 리뷰어 모델)을 묻고, 시작 지점은 존재하는 spec 문서에 따라 선택지를 걸러낸다. Step 1(작업 브랜치), Step 4/6(Approve / Request changes), Step 5(질문 재호출)도 `AskUserQuestion`을 사용한다.
- `.agents/workflows/plan-to-pr.js`: 검토 대상.
- `.agents/skills/create-pr/SKILL.md`: Step 68행 부근에서 `AskUserQuestion`을 사용하며, skill 7-2가 이를 참조한다 (선택지 수 제약이 동일하게 적용될 수 있음).

## Open Questions
1. FR-3의 "과거 실행에서 관찰된 오류" 출처가 정해지지 않았다. 사용자가 오류 내용을 직접 제공하는가, 아니면 세션 transcript/로그를 조사해야 하는가? 조사한다면 대상 범위(기간, 세션)는 어디까지인가?
2. FR-3의 체크리스트 기록 방식: 이슈 본문 수정인가, 댓글 추가인가? (이슈 수정은 외부 공개 변경이므로 승인 필요)
3. FR-3의 각 체크리스트 항목은 FR-2의 수정 범위에 포함되는가, 아니면 수정은 별도 이슈로 분리하는가?
4. 선택지가 2개 미만인 질문의 일반 규칙(FR-1)이 적용되는 범위: Step 2의 시작 지점 질문에 한정하는가, 모든 `AskUserQuestion` 사용처(Step 1, Gate, Step 5, `create-pr` skill 포함)에 적용하는가?
5. 선택지가 1개뿐일 때 "선택된 경로를 명시"하는 방식(채팅 메시지 한 줄 등)과 출력 언어가 정해지지 않았다.
6. 선택지가 0개 또는 1개가 되는 경우 중 시작 지점 외의 질문(예: 사용 가능한 옵션이 줄어드는 다른 질문)이 실제로 있는지 불명확하다. 있다면 각각의 기본 경로는 무엇인가?
7. FR-2의 "다른 제약 위반"에 포함할 제약의 범위: `AskUserQuestion` 제약(질문당 선택지 2~4개, 호출당 질문 수 상한 등)만인가, Workflow 도구 args 형식 등 다른 도구 제약도 포함하는가?
8. `AskUserQuestion`의 질문 수 상한 등 도구 제약의 정확한 수치는 이슈에 없다. 기준이 되는 도구 스키마 출처는 무엇인가?
9. 수정에 대한 검증 방식이 정해지지 않았다. skill/workflow는 단위 테스트 대상이 아니므로, 실제 재실행(예: `dryRun`)으로 확인할 것인가?
10. 이 이슈의 변경 결과를 PR로 낼 때 base 브랜치와 PR 생성 여부(수정 대상이 `.agents/` 하위 문서/스크립트)는 어떻게 하는가?

## Conflicts and Notes
- 이슈에 댓글과 부모 이슈가 없어 충돌하는 출처는 없다.
- 이슈 본문에는 "알려진 오류"가 Step 2의 `InputValidationError` 1건뿐이다. 나머지는 FR-3 조사 결과에 따라 추가된다.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-17 | Initial draft | Body |
