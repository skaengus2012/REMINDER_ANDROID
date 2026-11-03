# 요구사항 분석: [Task] Remove layout-to-composition state write in home blur layers (#893)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/893
- 부모 이슈: 없음
- 라벨: Compose, Performance
- 생성일: 2026-11-01
- 최종 수정일: 2026-11-02
- 분석 기준점: 2026-11-01T16:56:25Z (댓글 없음, 이슈 `updatedAt`)

## 배경 및 문제
`HomeScreen.kt`의 상단(head) 및 하단(bottom) 블러 레이어는 `Modifier.onSizeChanged`로 높이를 얻어 `MutableState`(`computedHeightToPx`)에 기록한다. 이 기록은 Layout 단계에서 일어나고, 같은 상태를 Composition 단계에서 읽는다(예: `BottomActions`의 `if (computedHeightToPx != 0)`). 즉 단계를 거스르는 역방향 쓰기이다.

- 첫 프레임에는 `computedHeightToPx == 0`이라 블러 레이어가 컴포지션되지 않고, 두 번째 컴포지션 이후에 나타난다. 화면 최초 표시 시 깜빡이거나 튀어 보일 수 있다.
- `BottomBlurLayer`가 `matchParentSize`를 사용해 높이를 보고하는 Box의 크기에 영향을 주지 않으므로 무한 루프는 발생하지 않는다.
- 동일 패턴이 head 블러 레이어(`HeadBlurLayer`)와 `BottomActions`에도 존재한다.
- #382 작업 코드를 Jetpack Compose 성능 체크리스트(layout feedback loop)로 점검하던 중 발견되었으며, #382 범위 밖이라 그곳에서는 변경하지 않았다.
- 추가로 `BottomActions`의 `remember(contentHeight, bodyBottomPadding) { ... toPx() }` 블록이 `Density`를 key로 갖지 않는다. 밀도 변경 시 Activity가 재생성되므로 우선순위는 낮다.

## 목표
홈 화면 블러 레이어(head/bottom)에서 Layout -> Composition 역방향 상태 쓰기를 제거하고, 기존 스크롤 연동 alpha 동작은 그대로 유지한다.

## 범위
### 포함
- `HeadBlurLayer` 사용부(`HomeStatusBackground`)의 높이 전달 방식 개선
- `BottomBlurLayer` 및 `BottomActions`의 높이 전달 방식 개선
- `BottomActions`의 `remember` key에 `Density` 반영(또는 key가 불필요하도록 재구성)
- 기기에서 첫 프레임 깜빡임 실제 재현 여부 확인(사용자가 실제 기기에서 Maestro 테스트로 수행)
- `HeadBlurLayer`(core)의 시그니처 변경(예: alpha를 lambda로 받기)

### 제외
- 스크롤 연동 alpha 동작 변경
- #382 범위의 변경

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | 블러 레이어의 첫 프레임 깜빡임이 기기에서 실제로 보이는지 확인한다. 확인은 사용자가 실제 기기에서 Maestro 테스트로 수행한다. | 본문(Needs), 사용자 결정 |
| FR-2 | 깜빡임 확인 여부와 관계없이 `HeadBlurLayer`와 `BottomBlurLayer` 모두 `MutableState`를 거쳐 크기를 되돌려 전달하는 Layout -> Composition 쓰기를 제거하고, Layout 또는 Draw 단계에서 크기를 읽도록 변경한다(예: `Modifier.layout`, `Modifier.drawWithCache`). 이를 위해 core의 `HeadBlurLayer` 시그니처 변경(예: alpha를 lambda로 받기)을 허용한다. | 본문(Needs), 사용자 결정 |
| FR-3 | 기존 스크롤 연동 alpha 동작은 변경하지 않는다. | 본문(Needs) |
| FR-4 | `BottomActions`의 `Density` 처리는 Compose가 권장하는 방식으로 개선한다(key 추가 또는 key 불필요 구조 중 Compose 권장 방식). | 본문(Needs), 사용자 결정 |

## 비기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| NFR-1 | 기존 `:feature:home` 테스트가 통과해야 한다. 신규 회귀 테스트는 추가하지 않는다. | 본문(Needs), 사용자 결정 |
| NFR-2 | 기기 시각 확인은 사용자가 실제 기기에서 Maestro 테스트로 수행한다. | 본문(Needs), 사용자 결정 |

## 인수 조건
- [ ] 기기에서 첫 프레임 깜빡임 재현 여부가 확인되어 결과가 기록되었다.
- [ ] `HeadBlurLayer`/`BottomBlurLayer` 경로에서 `onSizeChanged` -> `MutableState` -> Composition 읽기 패턴이 제거되었다.
- [ ] 스크롤에 따른 alpha 동작이 변경 전과 동일하다.
- [ ] `BottomActions`의 `Density` 처리가 Compose 권장 방식으로 개선되었다.
- [ ] `:feature:home` 기존 테스트가 통과한다.
- [ ] 사용자가 실제 기기에서 Maestro 테스트로 시각 확인을 완료했다.

## 영향 범위
코드에서 확인된 내용만 기재한다.
- `feature/home/src/main/kotlin/com/nlab/reminder/feature/home/ui/HomeScreen.kt`
  - `HomeStatusBackground`: `onSizeChanged`로 `computedHeightToPx` 기록, alpha 계산의 `derivedStateOf`에서 `computedHeightToPx == 0`일 때 0f 반환
  - `BottomActions`: `onSizeChanged`로 `computedHeightToPx` 기록, `if (computedHeightToPx != 0)`로 `BottomBlurLayer` 조건부 컴포지션, `remember(contentHeight, bodyBottomPadding)` 내 `toPx()` 사용
  - `BottomBlurLayer`: `computedHeightToPx`와 offset으로 alpha를 계산
- `HeadBlurLayer`는 `com.nlab.reminder.core.androidx.compose.ui`에서 import됨(정의 모듈 변경 필요 여부는 미확인)
- 테스트: `feature/home/src/test` 존재, `androidTest`는 없음

## 미해결 질문
없음 (모두 결정됨, 변경 이력 참고)

## 충돌 및 참고
- 댓글과 부모 이슈가 없어 본문과 충돌하는 내용은 없다.
- 본문은 Layout 단계 쓰기와 Composition 단계 읽기가 어느 항목에서 발생하는지를 `BottomActions`를 예로 들었다. 코드상 `HomeStatusBackground`는 Composition이 아닌 `derivedStateOf` 내부(alpha 읽기)에서 해당 상태를 읽는다.

---

## 변경 이력
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-02 | 최초 작성 | 본문 |
| 2026-11-02 | 미해결 질문 5건 결정 반영: 깜빡임 미확인이어도 FR-2 수행, 기기 확인은 사용자가 Maestro로 수행, `HeadBlurLayer` 시그니처 변경 허용, `Density`는 Compose 권장 방식, 신규 회귀 테스트 불필요 | 사용자 결정(코디네이터 전달) |
