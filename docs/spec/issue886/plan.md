# 계획: [Task] Add issue-to-pr skill for issue-to-draft-PR automation with review gates (#886)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/886
- 요구사항: docs/spec/issue886/requirements.md
- 생성일: 2026-11-02
- 최종 수정일: 2026-11-02

## 맥락 및 목표
이슈 → PR 흐름(요구사항 분석 → 계획 → 구현 → 리뷰 → 검증 → PR)을 `/issue-to-pr <issue-number>` 한 번의 호출로 실행한다. 사람의 검토 게이트는 메인 세션의 스킬이 처리하고, 사용자 입력이 필요 없는 구간은 동적 워크플로우 `plan-to-pr`에 위임한다. 최종 검증은 사람이 Draft PR에서 수행한다.

## 요구사항
`requirements.md`의 FR-1~FR-15, NFR-2를 따른다(NFR-1은 폐기). 이 계획에서 확정한 결정은 아래와 같다.

| 결정 | 내용 | 출처 |
|------|------|------|
| 스킬 / 워크플로우 이름 | 스킬 `issue-to-pr`, 워크플로우 `plan-to-pr` | 사용자 확정 |
| 워크플로우 규모 | 역할 3개(구현자, 리뷰어, 마무리)로 통합. 조기 종료와 변경분 리뷰를 적용. 호출 수가 가이드라인(5개 미만)을 넘어도 허용하며 설정은 바꾸지 않음 | 사용자 확정 |
| 토론 라운드 기본값 | 2회 (사전 질문에서 변경 가능) | 사용자 확정 |
| 실패 동작 | 자체 테스트/최종 검증 실패 시 수정 1회 재시도, 그래도 실패하면 PR 없이 중단하고 원인 보고 | 사용자 확정 |
| 결과물 검증 | 스킬과 워크플로우 구현이 끝나면 Opus 에이전트로 올바른지 검토 | 사용자 요청 |

## 아키텍처 및 설계

### 구성
```
.agents/
├── skills/issue-to-pr/SKILL.md     # 진입점. 질문, 게이트, 워크플로우 호출 (메인 세션)
└── workflows/plan-to-pr.js         # 무인 구간 (동적 워크플로우)
```

### 스킬 `issue-to-pr` (메인 세션)
1. **Project Config 블록**(SKILL.md 상단 단일 위치, NFR-2): 테스트 명령(`./gradlew :<module>:test`), PR 스킬 경로(`.agents/skills/create-pr/SKILL.md`), spec 디렉터리(`docs/spec/issue<N>/`), UI QA 도구(Maestro), 워크플로우 경로.
2. **사전 질문**(FR-2, 항상 수행): 시작 지점(기존 `requirements.md` / `plan.md`가 있으면 재개 선택지 제공), 최대 토론 라운드(기본 2), Draft PR 생성 여부, 리뷰어 모델.
3. **요구사항 분석**(FR-3): 서브에이전트에 `create-gh-issue-analysis-report`의 SKILL.md 경로를 명시해 전달(FR-15). 이 스킬은 저장소가 아니라 사용자 전역(`~/.claude/skills/`)에 있으므로 경로도 Project Config에 둔다. 모호한 항목은 추측하지 않고 질문 목록으로 반환받아 사용자에게 보고(FR-14).
4. **게이트 1**(FR-4): 요구사항을 보여 주고 승인 / 수정 요청. 승인될 때까지 반복.
5. **계획 수립**(FR-5): 서브에이전트가 `create-plans` 기준으로 `plan.md`를 작성하며 테스트 전략(단위 테스트만 / 단위 테스트 + 자동 UI QA)을 반드시 포함. 서브에이전트는 사용자에게 질문할 수 없으므로 모호한 항목은 질문 목록으로 반환받아 스킬이 사용자에게 전달.
6. **게이트 2**(FR-6): 계획 승인 / 수정 요청 반복.
7. **워크플로우 사전 준비**: Draft PR 생성을 선택한 경우, `create-pr`이 사용자에게 묻는 항목을 워크플로우 시작 전에 스킬이 해결한다(아래 "create-pr 연동" 참고).
8. **워크플로우 호출**: `Workflow({ scriptPath, args })`. `args`는 사전 질문 답변과 Project Config를 담은 JSON 객체.
9. **결과 보고**: 워크플로우 결과(PR URL, 미해결 쟁점, 중단 사유)를 사용자에게 전달.

### 워크플로우 `plan-to-pr` (무인 구간)
호출 수: 기본 최소 3회(구현자, 리뷰어, 마무리). 라운드 상한 2회 소진 시 최대 7회.

| 단계 | 에이전트 | 내용 |
|------|----------|------|
| 1. 구현 | 구현자 | `plan.md` / `requirements.md` / `mockup/`을 경로로 읽고 태스크별 TDD 구현. 커밋 규칙(`docs/guidelines/commit-rules.md`)에 맞춰 커밋. 변경 모듈 한정 단위 테스트 실행(FR-10). 계획의 테스트 전략이 UI QA면 Maestro 실행, 디바이스가 없으면 건너뛰고 `manualUiCheckRequired` 표시(FR-11, FR-12). 실패 시 1회 수정 후 재실행, 그래도 실패하면 중단. |
| 2. 리뷰 루프 | 리뷰어(사용자가 고른 모델) / 구현자 | 리뷰어가 `{file, line, severity(blocking/suggestion), summary}` 구조화 결과 반환. 차단급 지적이 없으면 즉시 종료(조기 종료). 있으면 구현자가 수정 또는 반박 후, 리뷰어가 이전 지적과 수정 diff만 재리뷰. 상한 소진 시 남은 지적을 `unresolved`로 기록(FR-8, FR-13). |
| 3. 마무리 | 마무리 | `verification-before-completion` 기준으로 최종 검증 후 실패 시 1회 수정 재시도. Draft PR 생성을 선택했다면 `create-pr`로 생성하며 PR 본문에 `unresolved`와 "manual UI check required"를 기록(FR-9, FR-12, FR-13). 선택하지 않았다면 커밋까지만 남기고 종료. |

설계 원칙: 문서 내용을 붙여 넣지 않고 경로만 전달, 에이전트 간에는 구조화된 JSON만 교환(토큰 절감).

### create-pr 연동 (워크플로우의 질문 불가 제약 대응)
`create-pr`은 실행 중 사용자에게 base 브랜치, 원격 브랜치 이름, 라벨, 커밋 위반 처리 방식을 묻는다. 워크플로우는 질문할 수 없으므로:
- base 브랜치, 원격 브랜치 이름은 스킬이 워크플로우 시작 전에 `create-pr`의 규칙대로 결정해 `args`로 전달한다. (확정: SKILL.md Step 7-2)
- 작업 트리는 워크플로우 시작 전과 각 에이전트 종료 시점에 깨끗해야 한다(`create-pr`이 더러운 트리에서 질문하고 멈추기 때문). 스킬은 시작 전에 `git status --porcelain`이 비어 있는지 확인하고, 에이전트 프롬프트는 커밋 규칙대로 커밋하고 임시 파일을 지우도록 지시한다.
- 마무리 에이전트가 최종 검증 실패를 1회 수정하는 경우, 그 수정은 리뷰를 거치지 않는다. 의도된 동작이며 PR 검토 시 사람이 확인한다.
- 라벨은 `create-pr`의 추천 규칙으로 자동 선택하고 확인 단계를 생략한다고 마무리 에이전트에 명시한다.
- 규칙 위반이나 판단 불가 시에는 추측하지 않고 중단 후 보고한다(FR-14).

### 구현에 반영된 제안 (PR 검토에서 최종 확인)
| 항목 | 제안 |
|------|------|
| 워크플로우 위치 | `.agents/workflows/plan-to-pr.js`, 스킬이 `scriptPath`로 호출 (프로젝트에 `.claude`가 없고 모든 에이전트 자산이 `.agents/` 아래에 있음) |
| 사전 질문 외 선행 결정 | 위 "create-pr 연동"에 따라 Draft PR 생성 시 base 브랜치를 스킬이 시작 전에 확정 (이슈 본문의 4개 질문에 추가되는 사항) |
| 리뷰어 모델 선택지 | Opus(기본) / Sonnet / 세션 기본. 구현자는 세션 기본 모델 |
| UI 변경 판단 기준 | 계획 단계에서 diff 예상 범위에 Compose / 레이아웃 변경이 있으면 UI QA 전략을 검토 |
| Draft PR 생성 안 함 선택 시 | 최종 검증까지 수행하고 로컬 커밋으로 종료, 푸시하지 않음 |

## 태스크
1. `.agents/skills/issue-to-pr/SKILL.md` 뼈대 작성: frontmatter(`name`, `description`)와 Project Config 블록.
   - **Verify**: `grep -n "^name: issue-to-pr" .agents/skills/issue-to-pr/SKILL.md`. 테스트 명령, PR 스킬 경로, spec 디렉터리, UI QA 도구, 워크플로우 경로가 Project Config 블록에만 나타나는지 `grep`으로 확인.
2. 사전 질문 단계 작성(시작 지점 재개 판정, 라운드 기본 2, PR 생성 여부, 리뷰어 모델).
   - **Verify**: FR-2의 4개 질문이 모두 있고, 기존 `requirements.md` / `plan.md` 유무에 따른 시작 지점 분기가 문서에 명시되어 있는지 확인.
3. 요구사항 분석 서브에이전트 호출과 게이트 1(승인 / 수정 반복, 모호 시 중단 보고, 명시적 SKILL.md 경로 전달) 작성.
   - **Verify**: FR-3, FR-4, FR-14, FR-15 항목이 문서 내 해당 단계에 대응하는지 체크리스트로 확인.
4. 계획 수립 서브에이전트 호출(질문 불가 시 질문 목록 반환, 테스트 전략 포함)과 게이트 2 작성.
   - **Verify**: FR-5, FR-6 대응 확인. 테스트 전략 선택지(단위만 / 단위 + UI QA)가 계획 산출 지시에 포함되어 있는지 확인.
5. 워크플로우 사전 준비(base 브랜치 / 원격 브랜치 결정, `args` JSON 구성)와 호출, 결과 보고 단계 작성.
   - **Verify**: `args` 스키마(issue, rounds, createPr, reviewerModel, testStrategy, startCommit, baseBranch, remoteBranch, config)가 SKILL.md와 워크플로우 스크립트에서 동일한지 대조.
6. `.agents/workflows/plan-to-pr.js` 뼈대 작성: `meta`, `args` 검증, JSON schema 정의(구현 결과, 리뷰 지적, 마무리 결과).
   - **Verify**: 스크립트를 async 함수로 감싼 임시 `.mjs`를 만들어 `node --check`로 구문 검사(스크래치패드에서 실행). 스크립트가 최상위 `return`을 쓰고 `export const meta`는 함수 안에서 쓸 수 없으므로, 감쌀 때 `export`를 떼어낸다(`(async () => {` + `const meta ...` + 본문 + `})()`). 원본을 그대로 `.mjs`로 검사하면 `Illegal return statement` 오류가 난다.
7. 구현 단계 작성: 구현자 프롬프트(경로 전달, TDD, 커밋 규칙, 변경 모듈 단위 테스트, UI QA 분기, 디바이스 없음 처리, 1회 재시도 후 중단).
   - **Verify**: `node --check` 통과. FR-10~FR-12와 실패 동작 결정이 코드에 대응하는지 확인.
8. 리뷰 루프 작성: 구조화 리뷰, 조기 종료, 변경분 재리뷰, 라운드 상한과 `unresolved` 기록.
   - **Verify**: `node --check` 통과. 라운드 상한이 `args.rounds`를 따르고 호출 수 최대치(7회)를 넘지 않는지 코드를 추적해 확인.
9. 마무리 단계 작성: 최종 검증(1회 재시도), `create-pr` 호출(질문 불가 지시, PR 본문 기록), PR 미생성 경로.
   - **Verify**: `node --check` 통과. PR 본문에 `unresolved`와 "manual UI check required"가 들어가는 경로를 코드에서 확인.
10. 스모크 테스트: `args.dryRun`으로 에이전트가 파일을 수정하지 않고 스키마에 맞는 더미 결과만 반환하게 하여 `args` 전달, 리뷰어 모델 옵션, 루프 제어를 실제 워크플로우 실행으로 검증.
    - **Verify**: `Workflow({ scriptPath, args: { ..., dryRun: true } })` 실행 결과에서 `args` JSON이 스크립트에 도달하고(이슈의 "확인할 항목" 해소) 루프가 조기 종료 / 상한 종료 양쪽에서 정상 동작하는지 확인.
11. Opus 검토: `model: opus` 에이전트에게 `requirements.md`, `plan.md`, `SKILL.md`, `plan-to-pr.js`를 읽히고 요구사항 충족 여부, 스킬과 워크플로우 사이 `args` 불일치, 질문 불가 제약 위반을 검토시킨다.
    - **Verify**: 검토 결과의 차단급 지적을 모두 반영하고 해당 태스크의 Verify를 다시 실행했을 때 통과. 반영하지 않은 지적은 사유를 변경 이력에 기록.

## 검증
- 요구사항 대응: FR-1~FR-15, NFR-2가 위 태스크의 Verify에서 각각 확인되었는지 마지막에 체크리스트로 대조한다.
- 구문: `plan-to-pr.js`는 태스크 6의 방식(async 함수로 감싼 임시 `.mjs`)으로 `node --check`를 통과해야 한다.
- 동작: 태스크 10의 `dryRun` 실행으로 `args` 전달과 루프 제어를 확인한다.
- 독립 검토: 태스크 11의 Opus 검토를 통과해야 한다.
- 실제 이슈 한 건에 대한 종단 간 실행은 사람이 수행한다. 이 작업의 범위에는 코드 변경 PR이 생기는 실행이 포함되지 않는다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-02 | 최초 작성 | requirements.md, 사용자 요청 |
| 2026-11-02 | 태스크 1~10 구현 완료. `dryRun` 스모크 테스트 2건 통과(1라운드 조기 종료 5회 호출, 상한 도달 시 `unresolved` 기록). 이로써 `args` JSON 전달이 검증됨 | 구현 결과 |
| 2026-11-02 | Opus 검토 반영: 시작 지점 선택지 4개 이하, `plan.md`의 `testStrategy:` 줄에서 값 읽기, 클린 트리 보장, 리뷰어가 `requesting-code-review`의 서브에이전트 생성·`gh` 코멘트 게시를 하지 않도록 제한, 리뷰어 막힘 보고(`failureReason`), 마무리 결과 검증, `headCommit` 정확성, `fullTestCommand`·`rounds` 필수화로 하드코딩 제거 | Opus 검토(태스크 11) |
| 2026-11-02 | 수정분에 대한 Opus 재검토는 사용자 결정으로 수행하지 않음. `dryRun` 재실행(정상 경로, 필수 키 누락 경로)과 구문 검사로 대체 | 사용자 요청 |
| 2026-11-02 | `AGENTS.md` 가이드 인덱스에 `issue-to-pr` 행 추가 | 사용자 요청 |
| 2026-11-02 | PR #888 Opus 리뷰의 경미한 지적 반영: 최종 검증·리뷰 수정 단계에 `fullTestCommand` 분기 추가, PR 본문의 미해결 쟁점에 구현자 응답(`implementerResponse`) 함께 기록, `reviewerModel` 값 검증, `SKILL.md`의 Gate 1·Gate 2 생략 조건 명시와 `testStrategy` 읽기를 항상 수행하도록 명확화, args 대조 목록에 `testStrategy`·`startCommit` 추가, 이미 구현된 제안 표의 제목을 현재 상태에 맞게 수정. 반영하지 않은 항목: `agent()`의 `effort` 옵션(dry-run 전용이며 오류 없이 통과), Project Config 밖 브랜치 패턴·전역 스킬 경로(공유 저장소 승격 시 설정 계약으로 처리, nlab-agent#26) | PR #888 Opus 리뷰(m1~m9) |
| 2026-11-02 | PR #888 Opus 리뷰 반영: `SKILL.md` Step 1에 작업 브랜치 확인 단계 추가(`master` 등에서 실행하면 중단하고 질문, `branch-rules.md` 준수). 구문 검사 방법을 정정: 원본을 `.mjs`로 복사하면 최상위 `return` 때문에 `Illegal return statement`가 나므로 async 함수로 감싸서 검사(스크립트는 정상, 위 "구문 검사 통과" 기록은 감싼 방식으로 재확인됨) | PR #888 Opus 리뷰(I1, I2) |
