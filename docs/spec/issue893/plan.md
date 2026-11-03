# Plan: Remove layout-to-composition state write in home blur layers (#893)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/893
- Requirements: docs/spec/issue893/requirements.md
- Created: 2026-11-02
- Last Updated: 2026-11-02

## 컨텍스트/목표
홈 화면의 head/bottom 블러 레이어는 `Modifier.onSizeChanged`로 얻은 높이를 `MutableState`(`computedHeightToPx`)에 기록하고, 이를 Composition(`BottomActions`의 `if (computedHeightToPx != 0)`) 및 `derivedStateOf`에서 읽는다. Layout -> Composition 역방향 쓰기이며, 첫 프레임에는 블러 레이어가 그려지지 않다가 두 번째 컴포지션부터 나타난다.

목표는 이 역방향 쓰기를 제거하고(Layout/Draw 단계에서 크기를 읽도록 변경), 스크롤 연동 alpha 동작은 그대로 유지하는 것이다. `BottomActions`의 `Density` 처리도 Compose 권장 방식으로 정리한다.

## 요구사항
requirements.md 기준.
- FR-1: 첫 프레임 깜빡임 실제 재현 여부 확인 (사용자가 실제 기기에서 Maestro로 수행)
- FR-2: `HeadBlurLayer`/`BottomBlurLayer` 모두 `MutableState`를 거친 크기 역전달 제거. core `HeadBlurLayer` 시그니처 변경 허용
- FR-3: 스크롤 연동 alpha 동작 불변
- FR-4: `BottomActions`의 `Density` 처리를 Compose 권장 방식으로 개선
- NFR-1: 기존 `:feature:home` 테스트 통과, 신규 회귀 테스트는 추가하지 않음
- NFR-2: 기기 시각 확인은 사용자가 실제 기기에서 Maestro로 수행

## 아키텍처/설계
코드 확인 결과:
- `HeadBlurLayer`(`core/androidx/compose`)는 내부에서 크기를 사용하지 않는다. `HomeStatusBackground`가 높이를 받는 목적은 `computedHeightToPx == 0`일 때 alpha를 0으로 만드는 것뿐이다. 따라서 head 쪽은 높이 상태를 제거하면 된다.
- `HeadBlurLayer`의 다른 사용처: `core/component/schedulelist/.../ScheduleListToolbar.kt` (`alpha = renderState.backgroundAlpha`).
- `BottomBlurLayer`는 높이를 alpha 계산(`derivedStateOf`)에만 사용한다.

설계 결정(요구사항에서 허용된 범위 내):
1. **Head**
   - `HeadBlurLayer`의 `alpha: Float`를 `alpha: () -> Float`로 변경한다. 값은 `drawBehind` 내부에서 읽어 Composition 재실행 없이 Draw 단계에서만 갱신한다.
   - `HomeStatusBackground`에서 `computedHeightToPx`, `onSizeChanged`, `mutableIntStateOf`를 제거한다. `derivedStateOf`는 스크롤/오프셋만 읽는다.
   - `ScheduleListToolbar`는 `alpha = { renderState.backgroundAlpha }`로 수정한다.
2. **Bottom**
   - `BottomActions`에서 `computedHeightToPx` 상태, `onSizeChanged`, `if (computedHeightToPx != 0)`를 제거한다. `BottomBlurLayer`는 항상 컴포지션된다.
   - `BottomBlurLayer`는 높이를 인자로 받지 않고, Draw 단계(`drawBehind` 또는 `drawWithCache`의 `DrawScope`)에서 `size.height`를 읽어 alpha를 계산한다. 기존 계산식(`computedHeightWithOffsetToPx`, `visibleClipToPaddingHeight` 등)은 `size.height`로 대체하되 수식은 동일하게 유지한다. `bodyScrollState.maxValue == Int.MAX_VALUE`이면 0f 처리도 유지한다.
   - `alphaAnimScrollTriggerOffsetToPx`는 `DrawScope`가 `Density`이므로 draw 안에서 `(bodyBottomPadding - contentHeight).toPx()`로 계산한다. 이로써 `remember` key 문제(FR-4)가 key 불필요 구조로 해소된다.
   - `BottomBlurLayer`의 파라미터는 `Dp` 값(`bodyBottomPadding`, `contentHeight`)과 `ScrollState`로 바꾼다. 라인(0.5dp)과 배경 두 레이어가 같은 alpha를 쓰므로 계산은 한 곳에서 하고 두 `drawBehind`에서 공유하는 방식(예: 하나의 `drawBehind` 안에서 두 번 그리기)을 사용한다.
3. 스크롤 상태는 draw 람다 안에서 읽으므로 스크롤 시 Recomposition이 발생하지 않는다(성능 개선, alpha 값 결과는 동일).

## 작업
1. `HeadBlurLayer`(core)의 `alpha`를 `() -> Float`로 변경하고 `drawBehind`에서 읽도록 수정, Preview와 `ScheduleListToolbar` 호출부를 함께 수정한다.
   - **Verify**: `./gradlew :core:androidx:compose:compileDebugKotlin :core:component:schedulelist:test`
2. `HomeStatusBackground`에서 `computedHeightToPx`/`onSizeChanged` 제거, alpha를 lambda로 `HeadBlurLayer`에 전달한다.
   - **Verify**: `./gradlew :feature:home:compileDebugKotlin`
3. `BottomBlurLayer`를 `size.height` 기반 Draw 단계 alpha 계산으로 재구성하고 `Dp` 입력을 받도록 변경한다(`Density` 처리 포함, `remember` 제거).
   - **Verify**: `./gradlew :feature:home:compileDebugKotlin`
4. `BottomActions`에서 `computedHeightToPx` 상태, `onSizeChanged`, 조건부 컴포지션을 제거하고 `BottomBlurLayer` 호출부를 갱신한다. 사용하지 않게 된 import(`onSizeChanged`, `mutableIntStateOf` 등)를 정리한다.
   - **Verify**: `./gradlew :feature:home:compileDebugKotlin` 이후 `grep -n "onSizeChanged\|computedHeightToPx" feature/home/src/main/kotlin/com/nlab/reminder/feature/home/ui/HomeScreen.kt` 결과가 없음
5. 변경 모듈 단위 테스트 실행 및 `:app:assembleDebug` 빌드 확인.
   - **Verify**: `./gradlew :feature:home:test :core:androidx:compose:test :core:component:schedulelist:test :app:assembleDebug`
6. 사용자 기기 확인 안내: 변경 전/후 첫 프레임 깜빡임 여부와 스크롤 alpha 동작을 사용자가 실제 기기에서 Maestro로 확인하고 결과를 기록하도록 전달한다(구현자가 수행하지 않음).
   - **Verify**: 사용자 확인 결과가 PR 설명 또는 이슈 댓글에 기록됨

## 검증
testStrategy: unit+ui

- 선택 근거: 변경이 Compose UI(블러 레이어의 draw/layout 방식)를 수정하므로 `unit+ui`로 한다. 단, requirements(NFR-2)에 따라 Maestro 기반 시각 확인은 사용자가 실제 기기에서 직접 수행한다. 구현 에이전트는 기기/에뮬레이터 UI QA를 자동 수행하지 않으며 Maestro 플로우 실행 결과를 단정하지 않는다.
- 단위 테스트: 변경 모듈 범위(`:feature:home`, `:core:androidx:compose`, `:core:component:schedulelist`)의 기존 테스트가 통과해야 한다. 신규 회귀 테스트는 추가하지 않는다(NFR-1).
- 빌드: `./gradlew :app:assembleDebug` 성공.
- 정적 확인: `HomeScreen.kt`에 `onSizeChanged` -> `MutableState` -> Composition 읽기 패턴이 남아있지 않음.
- 기기 확인(사용자, Maestro): 첫 프레임 깜빡임 재현 여부 기록, 스크롤 시 head/bottom alpha가 변경 전과 동일한지 확인.

## 미해결 질문
없음.

### 결정됨
- Maestro 플로우 파일은 저장소에 추가하지 않는다. 사용자가 실제 기기에서 임시로 확인한다.
- `HeadBlurLayer`의 `alpha`는 `() -> Float`로 단순 교체한다. `Float` 오버로드는 두지 않는다(draw 단계 읽기로 성능 확보).

---

## 변경 이력
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-02 | 최초 작성 | requirements.md |
| 2026-11-02 | 미해결 질문 2건 결정 반영: Maestro 플로우 저장소 미추가, `HeadBlurLayer` alpha 단순 교체(오버로드 없음). 미해결 질문 없음 | 사용자 결정(코디네이터 전달) |
