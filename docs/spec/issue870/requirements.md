# 요구사항 분석: [Docs] Make agents aware of the docs/spec issue document layout (#870)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/870
- Parent issue: None
- Labels: AX
- Created: 2026-10-30
- Last Updated: 2026-10-30
- Analysis Baseline: 2026-10-29T23:45:27Z

## 배경 및 문제
`create-gh-issue-analysis-report` 스킬은 `docs/spec/issue<number>/requirements.md`에 요구사항 분석 문서를 생성한다. 그러나 `AGENTS.md`의 Guidelines Index에는 `docs/spec` 구조와 문서의 목적이 언급되어 있지 않아, 다른 에이전트가 이 문서를 언제/어떻게 읽어야 하는지 알 수 없다.

현재는 요구사항 분석 문서만 사용 중이나, 향후 이슈별로 계획 문서와 구현 결과 요약 문서도 관리할 예정이다.

## 목표
에이전트가 `docs/spec/issue<number>/` 하위 문서의 존재, 구조, 역할, 읽는 시점을 `AGENTS.md`를 통해 알 수 있게 한다.

## 범위
### In Scope
- `AGENTS.md` Guidelines Index에 `docs/spec/issue<number>/requirements.md` 등록 (구현 전에 읽도록 안내)
- `docs/spec/issue<number>/` 하위 이슈별 문서 구조 및 문서 유형별 역할 정의
### Out of Scope
- 계획 문서 및 구현 결과 요약 문서를 생성하는 스킬의 신규 개발
- 계획 작성 스킬(`writing-plans`) 수정 및 이름 변경(`create-plans`), 산출물 위치·변경 이력 규칙 구현: nlab-agent 이슈 [skaengus2012/nlab-agent#24](https://github.com/skaengus2012/nlab-agent/issues/24)에서 진행

## 기능 요구사항
| ID | 요구사항 | Source |
|----|----------|--------|
| FR-1 | `AGENTS.md` Guidelines Index에 `docs/spec/issue<number>/requirements.md`를 등록하고, 에이전트가 구현 전에 읽어야 함을 명시한다. | Body |
| FR-2 | `docs/spec/issue<number>/` 하위 문서 구조를 정의한다. | Body |
| FR-3 | `requirements.md`의 역할을 요구사항 분석(현재 사용 중)으로 정의한다. | Body |
| FR-4 | 계획 문서의 역할을 작업 시점에 수립한 계획(향후 사용)으로 정의한다. | Body |
| FR-5 | 구현 결과 요약 문서의 역할을 실제 구현된 내용(향후 사용)으로 정의한다. | Body |
| ~~FR-6~~ | (Dropped: 스킬 쪽 규칙으로 이관, nlab-agent#24) | User request (chat) |
| ~~FR-7~~ | (Dropped: 스킬 쪽 규칙으로 이관, nlab-agent#24) | User request (chat) |

## 비기능 요구사항
N/A

## 인수 조건
- [x] `AGENTS.md` Guidelines Index에 `docs/spec/issue<number>/requirements.md` 항목과 읽는 시점(구현 전)이 있다.
- [x] `docs/spec/issue<number>/` 하위 문서 구조가 정의되어 있다.
- [x] `requirements.md`, 계획 문서, 구현 결과 요약 문서 각각의 역할이 정의되어 있고, 현재 사용 중/향후 사용 여부가 구분되어 있다.
- [x] 계획/결과 문서 생성 스킬은 포함되지 않았다.

## 영향 범위
- `AGENTS.md` (Guidelines Index), 신규 `docs/guidelines/spec-docs.md`
- `docs/spec/` (현재 저장소에는 디렉터리가 없으며, 스킬이 문서 생성 시 만든다)
- `docs/guidelines/` (현재 `branch-rules.md`, `commit-rules.md`만 존재)
- 계획 문서(`plan.md`)의 구조 정의는 본 이슈에서 하되, 생성 스킬 변경은 nlab-agent#24 소관

## 미해결 질문
- 계획/결과 문서를 인덱스에서 언제 읽도록 할지(읽는 시점)는 추후 결정한다. (보류)
- 문서 간 우선순위(요구사항 문서와 계획 문서가 어긋날 때의 기준)는 추후 결정한다. (보류)

## 충돌 및 참고사항
이슈에 댓글이 없고 부모 이슈도 없어 소스 간 충돌은 없다.

---

## 변경 이력
| 날짜 | 변경 내용 | Source |
|------|-----------|--------|
| 2026-10-30 | 초안 작성 | Body |
| 2026-10-30 | FR-6, FR-7 추가(계획 스킬 산출물 위치 및 변경 이력 규칙), 범위 갱신. 구조 정의 위치(`docs/guidelines/` 별도 파일 + `AGENTS.md` 인덱스), 계획/결과 파일명(`plan.md`/`result.md`), 문서 없을 때 건너뜀 방침은 사용자가 보류 항목(읽는 시점, 우선순위)만 남기고 진행하기로 하여 미해결 질문에서 제거 | User request (chat) |
| 2026-10-30 | FR-6, FR-7 Dropped(스킬 변경은 nlab-agent#24로 분리, 스킬은 `create-plans`로 개명 예정), 범위·인수 조건·영향 범위·미해결 질문 정리 | User request (chat) |
| 2026-10-30 | `docs/guidelines/spec-docs.md` 작성 및 `AGENTS.md` 인덱스 등록 완료(결과 문서 파일명은 `result.md`로 확정), 인수 조건 충족 표시 | 구현 |
