# Plan: [Bug] issue-to-pr skill/workflow errors on every run (#925)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/925
- Requirements: docs/spec/issue925/requirements.md
- Created: 2026-11-17
- Last Updated: 2026-11-17

## Context/Goals
`issue-to-pr` skill과 `plan-to-pr` workflow를 실행할 때마다 반복되는 오류를 제거한다. 확인된 대표 오류는 Step 2의 시작 지점 질문이 선택지 1개("From the beginning")만 남을 때 `AskUserQuestion`이 `InputValidationError`로 호출 전체를 거부하는 것이다. 수정 대상은 `.agents/` 하위 문서/스크립트뿐이며 앱 코드(Gradle 모듈)는 변경하지 않는다.

## Requirements
요구사항 문서의 FR-1 ~ FR-3을 따른다.

- FR-1: 선택지가 2개 미만인 질문은 하지 않고, 선택된 경로를 한 줄로 명시한 뒤 나머지 질문만 묻는다.
- FR-2: skill의 나머지 단계(Gate 1/2, Step 7 준비)와 `plan-to-pr.js`에서 제약 위반/반복 실패를 찾아 수정한다.
- FR-3: 과거 실행에서 관찰된 오류를 체크리스트로 정리한다.

### 가정 (Open Questions 수용에 따른 보수적 기본값)
| # | 관련 Open Question | 가정 |
|---|--------------------|------|
| A1 | 4 | FR-1 규칙은 Step 2 한정이 아니라 `issue-to-pr`의 모든 `AskUserQuestion` 사용처(Step 1, 2, 4, 5, 6, 7-1, 7-2)와 `create-pr` skill의 `AskUserQuestion` 사용처에 공통 적용한다. |
| A2 | 5 | 선택된 경로 명시는 채팅 메시지 한 줄, 한국어로 한다 (예: "시작 지점: 처음부터 (기존 spec 문서 없음)"). |
| A3 | 6, 7, 8 | `AskUserQuestion` 제약은 질문당 선택지 2~4개, 호출당 질문 1~4개로 가정한다. 이 수치는 이 계획 작성 환경에서 도구 스키마로 직접 확인하지 못했고, skill에 이미 적힌 "최대 4 options"와 이슈의 "질문당 최소 2개" 설명에 근거한다. 구현 시 `AskUserQuestion`을 호출해 확인할 수 있으면 수치를 보정한다. |
| A4 | 7 | FR-2의 "다른 제약"은 `AskUserQuestion` 제약과 `Workflow` 도구 args 형식(JSON 객체, 타입)까지로 한정한다. 그 외 도구 제약은 범위 밖이다. |
| A5 | 1, 2, 3 | FR-3은 세션 transcript를 새로 조사하지 않는다. 이슈 본문의 알려진 오류와 이번 코드 검토에서 발견한 항목을 체크리스트 초안으로 만들어 이 plan.md에 둔다. 이슈 본문/댓글 수정은 외부 공개 변경이므로 이 계획에서 실행하지 않고, 사용자의 명시적 승인 후에만 한다 (Task 9). 체크리스트 항목의 수정은 FR-2 범위에 포함한다. |
| A6 | 9 | 검증은 단위 테스트 대신 정적 확인(JS 구문 검사, 문서 grep)과 `plan-to-pr` workflow의 `dryRun` 실행으로 한다. `dryRun`은 workflow 도구 사용 가능 여부에 달려 있으며, 불가하면 구문 검사로 대체하고 결과에 명시한다. |
| A7 | 10 | 이 변경은 `feature/issue925`에서 커밋하고, PR base는 `master`로 가정한다 (PR 생성 여부는 사용자가 이후 결정). |
| A8 | 2~4개 초과/미달 | 선택지가 필요한데 자연스러운 선택지가 2개 미만인 열린 질문은 `AskUserQuestion`을 쓰지 않고 일반 채팅 질문(자유 입력)으로 한다. 임의로 기본값을 채워 답을 추정하지 않는다. |

## Architecture/Design
### 코드 검토로 확인한 문제 (FR-2 / FR-3 체크리스트 초안)
- [x] E1 (이슈 본문 확인): Step 2 시작 지점 질문이 선택지 1개일 때 `InputValidationError`, 호출 전체 거부. (`.agents/skills/issue-to-pr/SKILL.md` Step 2)
- [x] E2: Step 2는 "항상 단일 `AskUserQuestion` 호출로 묻는다"고 되어 있어 위 규칙(질문 생략)과 충돌한다. 문구 정리 필요.
- [x] E3: Step 3/5에서 서브에이전트가 반환한 Open Questions를 "한 번의 `AskUserQuestion` 호출로 묶어" 묻게 되어 있다. 질문이 5개 이상이거나 자연스러운 선택지가 없는 질문이면 제약을 위반한다.
- [x] E4: Step 7-2의 `remoteBranch`/`baseBranch` "ask the user"가 선택지 개수 보장 없이 기술되어 있다.
- [x] E5: `create-pr` skill Step 2는 부모 브랜치를 결정하지 못하면 후보를 `master` 하나만 두고 `AskUserQuestion`을 호출하게 되어 선택지가 1개가 된다. (`.agents/skills/create-pr/SKILL.md` 68행 부근)
- [x] E6: `plan-to-pr.js`의 `validateArgs`는 `a.createPr`를 truthy로 검사하지만 실제 동작은 `args.createPr === true`로 판단한다. 문자열 `"true"` 등이 전달되면 검증은 통과하는데 PR 단계는 건너뛰어 불일치가 생긴다. `testStrategy`/`issue` 타입 검증도 느슨하다.
- [x] E7: workflow의 finisher 에이전트는 질문할 수 없으나 `create-pr` skill에는 사용자 확인 지점이 있다. `finishPrompt`가 이를 모두 덮는지(base 브랜치, 원격 브랜치, 커밋 규칙 위반 등) 검토하고 빠진 지점이 있으면 프롬프트에 결정을 추가하거나 `failed` 반환 규칙을 명시한다.

E7 감사 결과: `finishPrompt`에 create-pr의 모든 확인 지점(미커밋 변경, 이슈 번호 누락, base/remote 브랜치 미확정, 커밋 규칙 위반, 검증 실패, 라벨)을 명시하고 결정이 없으면 `failed`로 반환하도록 보완함.
검증 메모: `node`가 없어 `osascript -l JavaScript`로 `plan-to-pr.js` 구문 검사 통과. `Workflow` dryRun은 이 환경에서 실행 불가(미실행). 이슈 본문/댓글은 수정하지 않음.

### 수정 방향
1. `issue-to-pr/SKILL.md`에 "Asking the user" 공통 규칙 섹션을 추가한다: 질문당 선택지 2~4개, 호출당 질문 1~4개, 선택지 2개 미만은 질문 생략 + 한 줄 명시, 열린 질문은 자유 입력 채팅 질문(A8).
2. Step 2는 질문 목록을 동적으로 구성하도록 다시 쓴다. 시작 지점 선택지가 1개면 해당 질문을 목록에서 빼고 경로를 명시한 뒤 나머지 3개 질문만 묻는다.
3. Step 3/5의 질문 취합 규칙, Step 7-2의 질문 규칙을 공통 규칙에 맞춘다.
4. `create-pr/SKILL.md` Step 2에 같은 규칙을 반영한다 (후보가 1개면 질문 없이 `master`를 base로 명시하고 진행).
5. `plan-to-pr.js`의 입력 검증을 엄격하게 하고, `finishPrompt`의 누락된 결정을 보완한다.

## Tasks
1. `issue-to-pr/SKILL.md`에 "Asking the user" 공통 규칙 섹션 추가 (선택지 2~4개, 질문 1~4개, 2개 미만이면 생략하고 한 줄 명시, 열린 질문은 자유 입력). A1~A3, A8 반영.
   - **Verify**: `grep -n "Asking the user" .agents/skills/issue-to-pr/SKILL.md`로 섹션 존재 확인, 규칙 4가지가 모두 본문에 있는지 읽어서 확인.
2. `issue-to-pr/SKILL.md` Step 2를 동적 질문 목록 방식으로 수정 ("항상 단일 호출" 문구 정리, 시작 지점 선택지 1개면 생략 후 경로 명시). E1, E2 해결.
   - **Verify**: Step 2 본문에 시작 지점 선택지가 1개인 경우의 처리와 "나머지 질문만 묻는다"가 명시되어 있는지 확인. spec 문서가 없는 경우 / requirements만 있는 경우 / plan만 있는 경우 / 둘 다 있는 경우를 표로 정리해 각 경우의 선택지 수가 모두 2 이상이거나 생략 처리됨을 확인.
3. `issue-to-pr/SKILL.md` Step 3/5의 Open Questions 취합 규칙을 수정 (호출당 최대 4개로 분할, 선택지 없는 질문은 일반 채팅 질문). E3 해결.
   - **Verify**: Step 3, Step 5 본문에 분할 규칙과 자유 입력 규칙이 있는지 확인.
4. `issue-to-pr/SKILL.md` Step 1, 4, 6, 7-1, 7-2의 `AskUserQuestion` 사용처를 공통 규칙에 맞춰 점검/수정 (Step 7-2의 `remoteBranch`/`baseBranch` 질문의 선택지 구성과 1개일 때 처리 포함). E4 해결.
   - **Verify**: `grep -n "AskUserQuestion" .agents/skills/issue-to-pr/SKILL.md`로 모든 사용처를 나열하고 각 사용처가 선택지 2~4개를 보장하거나 생략 규칙을 참조하는지 표로 대조.
5. `create-pr/SKILL.md` Step 2의 부모 브랜치 질문에 선택지 개수 가드 추가 (후보가 `master` 하나뿐이면 질문 없이 명시하고 진행, 중복 제거 후 개수 판단). E5 해결.
   - **Verify**: `grep -n "AskUserQuestion" .agents/skills/create-pr/SKILL.md`의 모든 사용처가 가드를 갖는지 확인하고, 부모 결정 불가 케이스가 본문에 처리되어 있는지 확인.
6. `plan-to-pr.js` `validateArgs` 강화: `createPr`를 boolean으로 엄격히 검증(비-boolean이면 오류), `issue` 정수 검증, `testStrategy` 값 검증(`unit`, `unit+ui` 또는 미지정). 동작(`=== true`)과 검증 기준을 일치시킨다. E6 해결.
   - **Verify**: 구문 검사 `node -e "new (Object.getPrototypeOf(async function(){}).constructor)('args','agent','phase','log',require('fs').readFileSync('.agents/workflows/plan-to-pr.js','utf8').replace(/^export const meta/m,'const meta'))"`가 오류 없이 종료. 잘못된 `args`(예: `createPr: "true"`)에 대해 `aborted('Input', ...)` 경로가 되는지 코드를 읽어 확인.
7. `plan-to-pr.js` `finishPrompt`와 `create-pr/SKILL.md`의 사용자 확인 지점을 대조해 누락된 결정을 보완하고, 사용자 확인이 필요한 상황은 `failed`로 반환하도록 프롬프트에 명시. E7 해결. 감사 결과(발견 항목 또는 "누락 없음")를 체크리스트에 기록.
   - **Verify**: `create-pr/SKILL.md`의 질문/확인 지점 목록과 `finishPrompt`의 결정 항목이 1:1로 대응하는 표를 작성해 확인 (이 표는 최종 보고에 요약). 구문 검사 재실행.
8. 정적 확인 및 `dryRun` 검증: 구문 검사, 변경 문서의 규칙 일관성 확인, 가능하면 `Workflow` 도구로 `plan-to-pr.js`를 `dryRun: true`로 실행.
   - **Verify**: 구문 검사 종료 코드 0, `dryRun`이 `status: "pr_created"` 또는 `"committed"`로 완료 (`calls` 값 확인). 도구를 쓸 수 없으면 그 사실과 대체 검증을 결과에 명시.
9. FR-3 체크리스트 마무리: 체크리스트 항목(E1~E7과 Task 7 감사 결과)의 해결 여부를 이 문서에 반영하고, 이슈에 기록할 마크다운 초안을 최종 보고에 포함한다. 이슈 본문/댓글 수정은 사용자 승인 없이 하지 않는다.
   - **Verify**: 체크리스트의 모든 항목이 Task 2~7 중 하나에 대응하는지 확인. `gh issue edit`/`gh issue comment`가 실행되지 않았음을 확인.

## Verification
testStrategy: unit

- 변경 대상이 `.agents/` 하위 문서와 JS 스크립트뿐이므로 Compose/레이아웃 UI 변경이 없고, Gradle 모듈 변경도 없다. 따라서 UI QA(Maestro)는 계획하지 않고, 영향받는 Gradle 모듈이 없어 `./gradlew` 테스트 대상도 없다 (단위 테스트 대상 모듈 없음).
- 대신 다음으로 검증한다.
  - `plan-to-pr.js` 구문 검사 (Task 6의 `node -e` 명령).
  - `grep`으로 모든 `AskUserQuestion` 사용처를 나열하고 선택지 수 규칙 준수 여부 대조 (Task 4, 5).
  - 가능한 경우 `plan-to-pr` workflow `dryRun` 실행 (Task 8).
- 완료 조건은 요구사항의 Acceptance Criteria 3개 중 FR-1, FR-2 항목이 문서/스크립트에서 확인되는 것이다. FR-3 항목은 체크리스트 초안 작성까지가 완료이고, 이슈 기록은 사용자 승인 후 수행된다.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-17 | Initial draft | requirements.md |
| 2026-11-17 | Mark E1~E7 checklist resolved, add E7 audit result, replace Task 6/8 verification with verification notes | review feedback |
