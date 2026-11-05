# 요구사항 분석: Review usage of post in `ItemTouchHelper.Callback` (#679)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/679
- 부모 이슈: #601 (Make all scheduleList)
- 라벨: Feature
- 생성일: 2025-12-26
- 최종 수정일: 2026-11-04
- 분석 기준점: 2025-12-26T06:59:14Z

## 배경 및 문제
- 이슈 제목은 `ItemTouchHelper.Callback`에서 `post`를 사용하는 부분을 검토하라는 것이다.
- 본문은 한 문장이다: `post`를 사용한 뒤, 정리(clear) 단계에서 `removeCallbacks`가 호출되도록 보장해야 한다.
- 부모 이슈 #601의 본문은 체크리스트(`- [ ] #821`) 하나뿐이며 이 이슈의 요구사항을 보충하는 내용은 없다.
- 이슈와 부모 이슈 모두 댓글이 없다.

## 목표
- `ItemTouchHelper.Callback`에서 `post`로 예약한 작업이 정리 단계에서 `removeCallbacks`로 취소되도록 보장한다.

## 범위
### 포함
- `ScheduleListItemTouchCallback`에서 `post`를 사용하는 부분의 사용 현황 검토
- 정리 단계(`clearResource()`, `clearView()` 모두)에서 `post`로 예약한 작업에 대한 `removeCallbacks` 호출
- 취소를 위해 `Runnable` 참조를 필드로 보관하는 것 (사용자 확인으로 허용)

### 제외
- `ScheduleListItemTouchCallback` 이외 클래스의 `post`/`removeCallbacks` (`StickyHeaderHelper`, `MultiSelectTouchListener`, `ScheduleDetailsEditText` 등) (사용자 확인)

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `ItemTouchHelper.Callback`에서의 `post` 사용 현황을 검토한다. | 제목 |
| FR-2 | `post`를 사용한 뒤, 정리(clear) 단계에서 `removeCallbacks`가 호출되도록 보장한다. | 본문 |
| FR-3 | 검토 대상은 `ScheduleListItemTouchCallback`로 한정한다. | 사용자 답변 (2026-11-04) |
| FR-4 | "정리(clear) 단계"는 `clearResource()`와 `clearView()` 두 곳 모두에서 검토한다. | 사용자 답변 (2026-11-04) |
| FR-5 | `post`로 예약하는 작업은 `Runnable` 참조를 필드로 보관하여 `removeCallbacks`로 취소할 수 있게 한다. | 사용자 답변 (2026-11-04) |

## 비기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| - | N/A (명시된 비기능 요구사항 없음) | - |

## 인수 조건
- [ ] `ScheduleListItemTouchCallback`의 `post` 사용처가 모두 검토되었다.
- [ ] `post`로 예약한 작업이 `clearResource()`에서 `removeCallbacks`로 취소된다.
- [ ] `clearView()`에서의 `post`/`removeCallbacks` 필요성이 검토되었고 결과가 반영되었다.

## 영향 범위
코드에서 확인한 내용이다.
- `ItemTouchHelper.Callback` 계열 클래스는 `ScheduleListItemTouchCallback`(`ItemTouchHelper.SimpleCallback` 상속) 하나다.
  - `core/component/schedulelist/src/main/kotlin/com/nlab/reminder/core/component/schedulelist/ui/content/ScheduleListItemTouchCallback.kt`
- 이 클래스의 `post` 사용처는 `onSelectedChanged`의 `ACTION_STATE_IDLE` 분기(497행) 한 곳이다. `dragAnchorOverlay.post { ... removeView(v) }`로 드래그 미러 뷰 제거를 예약한다.
- 같은 클래스의 `clearResource()`(559행)는 `mirrorViewBindingPool.clear()`, `dragAnchorOverlay.removeAllViews()`, 애니메이터 취소 등을 하지만 `removeCallbacks`는 호출하지 않는다.
- 예약된 `Runnable`은 `post { }` 람다로 만들어져 참조를 보관하지 않는다.
- `clearResource()`의 호출 위치는 `ScheduleListFragment.kt:620` 하나다. (`AllScheduleFragment.kt`의 호출은 주석 처리된 코드이며 다른 클래스 `ScheduleItemTouchCallback`를 쓴다.)
- `clearView()`(392행)에는 현재 `post` 사용이 없다. 뷰홀더마다 호출되므로 `removeCallbacks` 대상은 검토가 필요하다.

## 미결 질문
- 이슈 라벨이 `Feature`이지만 내용은 안정성 개선에 가깝다. 사용자에게 보이는 동작 변경이 있는가?

## 충돌 및 참고
- 본문과 댓글 간 충돌 없음 (댓글 없음).
- 제목은 "검토"를, 본문은 "`removeCallbacks` 보장"을 말한다. 둘 다 요구사항으로 기록했다.
- 정리 이후 예약 작업이 실행될 때의 구체적 장애 시나리오는 없다. 사용자는 "좋지 않은 냄새(code smell)" 수준의 예방적 개선으로 설명했다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-04 | 최초 작성 | 본문, 부모 이슈 |
| 2026-11-04 | 영향 범위의 `clearResource()` 호출처에서 `AllScheduleFragment.kt` 제거(주석 처리된 죽은 코드, 다른 클래스) | 코드 재확인 |
| 2026-11-04 | 미결 질문 4건 해소: 정리 단계는 `clearResource()`/`clearView()` 모두, 범위는 `ScheduleListItemTouchCallback`, `Runnable` 필드 보관 허용, 장애 시나리오 없음. FR-3~FR-5 추가, 범위·인수 조건 갱신, `clearResource()` 호출 위치 보정 | 사용자 답변 |
