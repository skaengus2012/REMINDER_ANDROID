# 계획: Review usage of post in `ItemTouchHelper.Callback` (#679)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/679
- 요구사항: docs/spec/issue679/requirements.md
- 생성일: 2026-11-04
- 최종 수정일: 2026-11-04

## 맥락/목표
- `ScheduleListItemTouchCallback`은 드래그 종료 시 `dragAnchorOverlay.post { removeView(v) }`로 미러 뷰 제거를 예약한다(497행).
- 이 예약 작업은 `Runnable` 참조를 보관하지 않아 정리 단계(`clearResource()`)에서 취소할 수 없다. 장애 사례는 없고 예방적으로 개선하는 작업이다.
- 목표: 예약한 작업을 정리 단계에서 `removeCallbacks`로 취소할 수 있게 하고, `clearView()`에서의 필요성도 검토한다.

## 요구사항
- FR-1: `post` 사용 현황 검토
- FR-2: 정리 단계에서 `removeCallbacks` 호출 보장
- FR-3: 대상은 `ScheduleListItemTouchCallback`로 한정
- FR-4: `clearResource()`와 `clearView()` 모두 검토
- FR-5: `Runnable` 참조를 필드로 보관

## 아키텍처/설계
### 검토 결과 (코드 확인)
- `post` 사용처는 `onSelectedChanged`의 `ACTION_STATE_IDLE` + 이전 상태 `DRAG` 분기 1곳이다.
- 이 람다는 실행 시점에 다시 드래그 상태이고 같은 미러 뷰(`dragAnchorMirrorView === v`)이면 아무것도 하지 않고 종료한다.
- 새 드래그가 시작되면(454~456행) 다른 자식 뷰는 이미 제거되고, `clearResource()`는 `removeAllViews()`를 호출한다. 따라서 취소된 예약 작업이 남기는 뷰는 정리 단계에서 함께 제거된다.
- `clearView()`(392행)는 `post`를 쓰지 않고 `SwipeableViewHolder` 분기에서 `getDefaultUIUtil().clearView()`와 애니메이터 등록만 한다. 미러 뷰 생명주기와 무관하다. 또 `ItemTouchHelper`에서 `clearView()`는 드래그 복귀 애니메이션 종료 시 호출되므로 `onSelectedChanged(IDLE)`로 예약된 작업은 이미 실행됐거나 아직 생기기 전이다. 따라서 `removeCallbacks`를 추가하지 않는다. 이 결론은 코드에 반영하지 않고 검토 결과로만 남긴다(Task 1).
- 493행의 `.let`(비 `?.let`)은 미러 뷰 `v`가 null이어도 `post`를 한다. 이때 `removeView(null)`은 아무 일도 하지 않으므로 동작은 그대로 두고 범위 밖으로 둔다(Task 1에 기록).
- `clearResource()`의 호출처는 `ScheduleListFragment.kt:620` 하나다. `AllScheduleFragment.kt`의 호출은 주석 처리된 코드이며 다른 클래스(`ScheduleItemTouchCallback`)를 쓴다.

### 변경 설계
- 예약 작업을 `object : Runnable`로 만든다. SAM 람다 안의 `this`는 바깥 클래스를 가리키므로 스스로 set에서 빠지려면 `object : Runnable`이어야 한다.
- 드래그 종료가 연속될 수 있으므로 단일 필드가 아니라 `private val pendingMirrorViewRemovals = mutableSetOf<Runnable>()`로 보관한다. 단일 필드를 쓰면 `v`가 null인 예약이 이전 예약을 덮어써 취소 불가능한 작업이 남을 수 있다.
- 주 수단은 `clearResource()`에서 set의 모든 `Runnable`에 대해 `dragAnchorOverlay.removeCallbacks(it)`를 호출하고 set을 비우는 것이다(이슈 본문 요구). 순서는 `removeAllViews()` 앞에 둔다.
- 보조 안전장치로 self-guard를 둔다. `run()` 시작 시 `if (!pendingMirrorViewRemovals.remove(this)) return`. `View.removeCallbacks`는 overlay가 detach된 상태에서는 Handler 큐의 예약을 지우지 못하므로, 큐에 남은 예약이 늦게 실행돼도 아무 일도 하지 않게 한다. 정확성이 아니라 "취소 보장"의 의미를 지키기 위한 장치다(정리 후 늦게 실행돼도 `removeAllViews()`로 이미 뷰가 없어 해가 없다).

```kotlin
val removal = object : Runnable {
    override fun run() {
        if (pendingMirrorViewRemovals.remove(this).not()) return
        if (selectedActionState == ItemTouchHelper.ACTION_STATE_DRAG && dragAnchorMirrorView === v) return
        dragAnchorOverlay.removeView(v)
    }
}
pendingMirrorViewRemovals += removal
dragAnchorOverlay.post(removal)
```
- 기존의 `post` 사용 이유(레이아웃 패스 중 `removeView`로 인한 `IllegalStateException` 회피)와 가드 조건은 그대로 유지한다. 동작 변경은 없다.

## 작업
수행 순서(TDD): Task 1 → Task 5 → Task 2 → Task 3 → Task 4 → Task 6 → Task 7

1. 현재 `post`/`removeCallbacks` 사용 현황을 다시 확인하고 검토 결과를 정리한다. 결과에는 다음을 기록한다: `clearView()`는 변경하지 않음(위 근거), 493행 `.let`은 `v`가 null이어도 `post`되지만 no-op이므로 유지, `clearResource()` 호출처는 `ScheduleListFragment.kt:620` 하나.
   - **Verify**: `grep -nE "\.post|removeCallbacks|postDelayed" core/component/schedulelist/src/main/kotlin/com/nlab/reminder/core/component/schedulelist/ui/content/ScheduleListItemTouchCallback.kt` 결과가 497행 한 곳임을 확인
2. `ScheduleListItemTouchCallback`에 `pendingMirrorViewRemovals` 필드를 추가하고, 497행의 `post` 람다를 `object : Runnable`로 바꿔 set에 등록한 뒤 `post(removal)`한다. self-guard와 기존 가드 조건을 유지한다.
   - **Verify**: `./gradlew :core:component:schedulelist:compileDebugKotlin`
3. `clearResource()`에서 `removeAllViews()` 호출 전에 `pendingMirrorViewRemovals`를 순회하며 `dragAnchorOverlay.removeCallbacks(it)`를 호출하고 set을 비운다.
   - **Verify**: `./gradlew :core:component:schedulelist:compileDebugKotlin`, Task 5의 테스트 통과
4. 모듈 전체 단위 테스트, 앱 빌드, 정적 규칙으로 회귀 여부를 확인한다.
   - **Verify**: `./gradlew :core:component:schedulelist:test`, `./gradlew :app:assembleDebug`, 파일 내 `\.post\s*[({]`가 `post(removal)` 한 곳이고 `pendingMirrorViewRemovals`의 `+=`와 `removeCallbacks`가 짝을 이루는지 grep으로 확인
5. (Task 2보다 먼저 수행) `ScheduleListItemTouchCallbackTest`를 `core/component/schedulelist/src/test/kotlin/com/nlab/reminder/core/component/schedulelist/ui/content/`에 작성한다. `mockk<FrameLayout>(relaxed = true)`를 overlay로 쓰고 `onSelectedChanged(null, DRAG)` → `onSelectedChanged(null, IDLE)`로 `post`를 유도한다. 테스트는 둘로 나눈다.
   - `clearResource_removesPostedCallback`: `post`에 넘긴 `Runnable`과 `clearResource()` 후 `removeCallbacks`에 넘긴 `Runnable`이 같은 객체다.
   - `postedCallback_doesNotRemoveView_afterClearResource`: `clearResource()` 후 늦게 `run()`해도 `removeView`가 호출되지 않는다. (`any()`가 null 인자를 매칭하는지 불확실하므로 `allAny()`를 쓰거나 처음 실행에서 실패가 실제로 나오는지 확인한다.)
   - **Verify**: 구현 전(Task 2 이전)에 `./gradlew :core:component:schedulelist:test --tests '*ScheduleListItemTouchCallbackTest'`가 컴파일되고 두 테스트 모두 실패하며, Task 3 이후 통과
6. (Task 4 이후) Maestro로 비타이밍 시나리오(아래 검증 섹션의 M1·M2·M3·M4)를 실행하고 결과(통과/실패, 기기)를 기록한다. flow 파일은 저장소에 커밋하지 않는다(범위 밖). 드래그 프로브 결과(2026-11-04, 구현 전 코드, 실기기): 일반 모드에서는 드래그 핸들이 없고 길게 눌러 드래그하는 방식이라 Maestro `swipe`로는 드래그가 시작되지 않았다(순서 불변). `adb shell input draganddrop <x1> <y1> <x2> <y2> 1500`은 드래그가 실제로 동작해 순서가 바뀌었고 반대로 드래그해 원복도 확인했다. 따라서 드래그 동작은 `adb draganddrop`으로, 회전·다크모드·`back`·화면 검증은 Maestro로 나눠 수행한다.
   - 실행 결과(2026-11-04, 구현 후 빌드를 `adb install -r`로 설치한 실기기, 앱 프로세스 재시작 없음): M1 통과(드래그 후 순서 변경, 반대 드래그로 원복, 미러 뷰 잔상 없음), M2 통과(12회 연속 드래그·원복 후 순서·화면 동일, `logcat`에 `FATAL EXCEPTION`/`IllegalStateException` 없음), M3 통과(드래그 후 가로 회전→세로 복귀, 다크모드 on→off 후에도 순서 유지, 크래시·잔상 없음), M4 통과(`back` 후 재진입 3회 반복, 이후 드래그 정상). 테스트 후 다크모드·회전 설정과 아이템 순서는 원래대로 되돌렸다.
   - 한계: 이 확인은 회귀·크래시 여부만 보여 준다. `removeCallbacks`가 실제로 취소했는지는 화면으로 구분되지 않는다.
   - **Verify**: 각 시나리오가 통과하고 앱이 종료되지 않음. 실패 시 원인을 PR에 기록
7. (Task 4 이후) 타이밍 시나리오(아래 검증 섹션의 H1·H2·H3)를 사람이 기기에서 확인하고 결과를 PR 본문에 기록한다. 재현되지 않으면 "재현 시도했으나 타이밍상 보장 불가"라고 그대로 적는다.
   - **Verify**: PR 본문의 체크리스트에 각 시나리오의 결과가 기록됨

## 검증
- 1차 검증은 정적 검증이다. 추가로 기기 확인을 둔다: 타이밍에 의존하지 않는 시나리오는 Maestro, 타이밍에 의존하는 시나리오는 사람이 수행한다(사용자 결정). `removeCallbacks`의 실제 취소 여부는 화면으로 구분되지 않으므로 이 확인들은 회귀·크래시 확인 용도다.
- 이 모듈에는 UI 테스트와 Robolectric 설정이 없다. 대신 `testkit`이 `api`로 제공하는 mockk로 순수 JVM 단위 테스트를 작성한다. AGP mockable `android.jar`에서 interpolator 생성자가 no-op임을 확인했고, `onSelectedChanged(null, …)` 경로는 실제 Android 메서드를 호출하지 않는다. 첫 실행에서 `FrameLayout` 모킹이 동작하는지는 Task 5에서 확인한다.
- 검증 수단: (1) 컴파일 (2) grep 규칙 (3) 코드 리딩 체크리스트: `post`와 `removeCallbacks`에 같은 객체를 넘기는가, `object : Runnable`인가, `clearResource()`가 `clear()`하는가, `clearView()`가 변경되지 않았는가 (4) mockk 단위 테스트 (5) `:core:component:schedulelist:test`, `:app:assembleDebug`
- 기기 확인 시나리오 (결과는 PR 본문에 체크리스트로 기록):

  | ID | 시나리오 | 수행 | 기대 결과 |
  |----|----------|------|-----------|
  | M1 | 기본 드래그 회귀: 아이템 드래그 후 놓기 | Maestro | 미러 뷰 잔상 없음, 순서 정상 반영 |
  | M2 | 빠른 반복: 드래그→놓기 10회 이상 연속 | Maestro | 잔상/중복 아이템·크래시 없음 |
  | M3 | 구성 변경: 드래그→놓기 후 화면 회전 또는 다크모드 전환 | Maestro | 크래시·잔상 없음 |
  | M4 | 화면 재진입: 목록 화면을 나갔다 다시 들어와 드래그 | Maestro | 정상 동작 |
  | H1 | 놓자마자 재드래그: 놓은 직후 같은 핸들을 다시 길게 눌러 드래그 | 사람 | 재드래그 중 미러 뷰가 사라지지 않음(498행 가드 회귀) |
  | H2 | 놓은 직후 화면 이탈: 드래그→놓기 직후 뒤로가기 또는 화면 이동 | 사람 | 크래시 없음 |
  | H3 | 드래그 도중 화면 이탈: 드래그 중 뒤로가기/홈 버튼 | 사람 | 크래시 없음, 복귀 후 잔상 없음 |

- 타이밍 시나리오(H1~H3)를 Maestro가 아닌 사람이 수행하는 이유: `post`는 다음 프레임(약 16ms)에 실행되고 드롭 복귀 애니메이션도 약 200ms인데, Maestro는 명령 사이 지연이 수백 ms 이상이라 해당 구간을 노릴 수 없다. H3은 `swipe`가 원자적 제스처라 중간에 끼어들 수 없다.
- 선택 사항(로컬 한정, 커밋하지 않음): `Runnable.run()` 시작과 `clearResource()`에 임시 `Timber.d`를 넣어 정리 이후 `run`이 실행되는지(실행되어도 self-guard에서 return하는지) 관찰할 수 있다.
- 한계: 실제 `Handler`/`RunQueue`의 동작은 검증하지 못하고 "같은 객체로 취소를 요청한다"는 계약만 검증한다. `clearResource()` 이후 새로 생기는 `post`는 취소되지 않지만 `removeView`가 no-op이라 해가 없다.
- 인수 조건 대응: `post` 사용처 검토와 `clearView()` 결과 반영(Task 1), `clearResource()`에서 취소(Task 2~3, 5), 회귀 확인(Task 4, 6, 7).

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-04 | 최초 작성 | requirements.md |
| 2026-11-04 | 기기 확인 추가: 비타이밍 시나리오 M1~M4는 Maestro(Task 6), 타이밍 시나리오 H1~H3는 사람(Task 7)이 수행하고 PR에 기록. 검증 섹션의 "정적 검증만" 문구 수정 | 사용자 결정 |
| 2026-11-04 | 정적 검증 중심으로 정리하고 Opus 3회차 리뷰 반영: `object : Runnable` + self-guard 추가, `clearView()` 불필요 근거 교체, 호출처 보정, 수동 검증 삭제, mockk 단위 테스트 Task 5 추가(TDD 순서 명시), Task 4 검증에 grep 규칙 추가 | 사용자 요청, 리뷰 |
