# 요구사항 분석: [Task] Run CI on pull requests targeting epic branches (#880)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/880
- 부모 이슈: 없음
- 라벨: buildScript
- 생성일: 2026-11-01
- 최종 수정일: 2026-11-01
- 분석 기준점: 2026-11-01T09:37:54Z

## 배경 및 문제
- `.github/workflows/ci.yml`의 `pull_request` 트리거가 `master` 브랜치만 대상으로 한다.
- 브랜치 규칙에 따르면 feature 브랜치는 `epic/issue<issue-number>` 브랜치로 Pull Request를 통해 머지되는데, 이 PR에서는 CI가 실행되지 않는다.
- 이슈에 댓글은 없다.

## 목표
- `epic/**` 브랜치를 대상으로 하는 PR에서도 CI가 실행되도록 한다.
- 기존 `master` 대상 PR의 CI 동작은 유지한다.

## 범위
### 포함
- `pull_request` 트리거의 대상 브랜치 확장 (`epic/**`).

### 제외
- `push` 트리거 변경: PR이 열려 있는 동안의 push는 `pull_request` 트리거(`synchronize`)가 처리하므로 `master` 한정을 유지한다. (사용자 전달 사항)

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `epic/**` 브랜치를 대상으로 하는 Pull Request가 열리거나 갱신되면 CI가 실행되어야 한다. | 본문 (Needs) |
| FR-2 | `master` 브랜치를 대상으로 하는 Pull Request에서도 CI가 계속 실행되어야 한다. | 본문 (Needs) |
| FR-3 | `epic/**` 대상 PR의 CI는 `master` 대상 PR과 동일한 단계(빌드, 커버리지/테스트 리포트 업로드, Slack 알림 등)를 수행해야 한다. | 사용자 전달 사항 |

## 비기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| - | 명시된 비기능 요구사항 없음 | - |

## 인수 조건
- [ ] `epic/**` 브랜치를 대상으로 하는 PR에서 CI 워크플로가 실행된다.
- [ ] `master` 브랜치를 대상으로 하는 PR에서 CI 워크플로가 계속 실행된다.
- [ ] `epic/**` 대상 PR에서 `master` 대상 PR과 동일한 CI 단계가 수행된다.

## 영향 범위
- `.github/workflows/ci.yml`: 현재 `on.pull_request.branches: [ master ]`로 되어 있다. (`on.push.branches`도 `[ master ]`) codecov 업로드 등 이후 단계는 브랜치 조건 없이 `matrix.buildType`으로만 분기하므로, 트리거만 넓히면 epic PR에도 동일하게 적용된다.
- `docs/guidelines/branch-rules.md`: epic 브랜치 명명 규칙(`epic/issue<issue-number>`)과 PR 흐름을 정의한다. (참고용)

## 미결 질문
- 없음

## 충돌 및 참고
- 댓글이 없어 본문과 댓글 간 충돌은 없다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-01 | 최초 작성 | 본문 |
| 2026-11-01 | 브랜치 패턴을 `epic/**`로 확정, epic PR도 `master` PR과 동일한 CI 단계 수행(FR-3 추가), 해당 미결 질문 2건 해소 | 사용자 전달 사항 |
| 2026-11-01 | `push` 트리거는 변경하지 않기로 확정(PR이 있을 때의 push는 `pull_request`가 커버), 미결 질문 해소 | 사용자 전달 사항 |
