# Plan: [Task] Optimize home blur layer rendering (#896)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/896
- Requirements: docs/spec/issue896/requirements.md
- Created: 2026-11-03
- Last Updated: 2026-11-03

## Context/Goals
- Home 화면의 블러 레이어(`BottomBlurLayer`, `HeadBlurLayer`)가 스크롤 중 불필요하게 다시 그려지고 노드 수가 많은 문제를 개선한다.
- 시각적 결과는 현재와 동일하게 유지한다 (NFR-1). 부모 이슈 #895의 "기존 UI 동작 유지" 원칙을 따른다.
- 변경 전/후 효과를 측정해 PR에 기록한다 (NFR-2).

## 현재 코드 상태 (재점검 결과, 기준: `origin/epic/issue895` = PR #894 머지 포함)
- `HeadBlurLayer`: 이미 `Spacer + drawBehind` 1노드이고 `alpha: () -> Float`(draw 단계에서만 읽고 0..1 coerce)이며, 선은 `0.5.dp.roundToPx()` 반올림 px `drawRect`다. -> **FR-2 완료, FR-3의 HeadBlurLayer 쪽 완료.**
- `BottomBlurLayer`: 이미 `Spacer + drawBehind` 1노드이고 선은 `0.5.dp.roundToPx()` 반올림 px `drawRect`다. -> **FR-3 완료(두 레이어가 같은 방식).** 다만 `drawBehind` 안에서 `bodyScrollState.value`를 직접 읽어 알파가 상수인 구간에도 스크롤 프레임마다 전체를 다시 그린다. -> **FR-1만 남음.**
- `HomeStatusBackground`는 `derivedStateOf` 알파를 `{ alphaState }` 람다로 `HeadBlurLayer`에 전달한다. `ScheduleListToolbar`는 `alpha = { renderState.backgroundAlpha }`로 사용한다. 두 사용처 모두 수정 불필요.
- 따라서 이번 이슈의 실제 코드 변경 범위는 `BottomBlurLayer` 한 곳이다.

## 가정
1. **기준 브랜치**: `feature/issue896`(= `origin/epic/issue895` 기반, #894 포함)에서 작업한다.
2. **벤치마크**: 새 Macrobenchmark 모듈은 추가하지 않는다. 저장소에 코드를 남기지 않는 일회성 로컬 측정(Home 스크롤 시나리오, `adb shell dumpsys gfxinfo <pkg> framestats`/`reset`으로 jank 비율과 프레임 시간 비교)으로 변경 전/후를 비교하고 결과만 PR 본문에 기록한다.
3. **공개 API**: `HeadBlurLayer` 시그니처는 변경하지 않는다. 코드상 변경이 필요하지 않다. `BottomBlurLayer`는 private이라 자유롭게 바꾼다.
4. **FR-4(선택)**: 이번 PR 범위에서 제외한다. 별도 후속으로 남긴다. 현재 Compose runtime이 1.12.1(BOM 2026.09.00, 최신 stable)이라 `computedStateOf`(Compose 1.13+)를 사용할 수 없다. 1.13은 beta(`1.13.0-beta02`)까지만 나와 있어, 1.13 stable과 BOM이 나오면 의존성 갱신과 함께 별도 이슈로 재검토한다. 그때까지 `HomeStatusBackground`에 TODO 주석을 남긴다.
5. **시각 동일성 확인**: 변경 전/후 스크린샷 비교(Home 스크롤 상단/중간/하단). 자동 스크린샷 테스트는 추가하지 않는다.
6. **병합 순서**: 같은 파일을 다루는 다른 #895 하위 PR과의 충돌은 PR 단계에서 해결한다.

## Requirements
- FR-1: `BottomBlurLayer`는 정적 rect를 한 번만 그리고 알파는 `Modifier.graphicsLayer`(`CompositingStrategy.ModulateAlpha`)로만 갱신한다. draw op별 알파(컨테이너 0.925, 선 1.0)는 유지한다. **(남은 작업)**
- FR-2: 완료(#893). 코드 변경 없음, 현상 확인만.
- FR-3: 완료(#893). 코드 변경 없음, 두 레이어가 동일 방식임을 확인만.
- FR-4: 제외 (가정 4).
- NFR-1: 시각적 결과 동일. NFR-2: 변경 전/후 측정 결과 기록.

## Architecture/Design
### BottomBlurLayer (`feature/home/.../ui/HomeScreen.kt`)
- `Spacer(modifier.graphicsLayer { alpha = <계산>; compositingStrategy = CompositingStrategy.ModulateAlpha }.drawBehind { ... })`.
- 알파 계산(기존 로직 그대로: `maxValue == Int.MAX_VALUE`면 0, 아니면 남은 스크롤 기반 1 - 비율)은 `graphicsLayer` 람다로 옮긴다. `GraphicsLayerScope.size.height`와 `Dp.toPx()`(GraphicsLayerScope는 `Density`)를 사용하므로 기존의 `size`/`toPx` 의존이 유지된다. 알파가 같으면 레이어 속성이 바뀌지 않아 invalidation이 없고, 알파가 0 또는 1로 일정한 구간에서는 레이어 갱신도 일어나지 않는다.
- `drawBehind`는 스크롤 상태를 읽지 않는다. 컨테이너 `containerColor.copy(alpha = 0.925f)` 전체 rect와 `lineColor`(알파 1.0) 상단 `0.5.dp.roundToPx()` px rect만 그린다. `ModulateAlpha`는 레이어 알파를 각 draw op 알파에 곱하므로 기존 `0.925f * alpha`, `1.0f * alpha`와 동일하며 오프스크린 버퍼가 생기지 않는다.
- 계산식은 읽기 쉽도록 `graphicsLayer` 안에서 로컬 함수/변수로 정리하되 동작은 바꾸지 않는다. `derivedStateOf`는 도입하지 않는다(람다가 layer 단계에서 스크롤 상태를 직접 읽는 것으로 충분).
- `HeadBlurLayer`는 알파가 0..1로 연속 변하므로 이번 이슈에서는 현행 유지한다(이슈 범위 밖).

### 0 나누기(NaN) 방어 (PR #894 정적 분석 노트 반영)
- 배경: `BottomBlurLayer`의 `bottomContainerAnimPx / maxBottomContainerAnimPx`는 `alphaAnimScrollTriggerOffset`이 0px이면 `0 / 0f = NaN`이 되고, `coerceIn`은 NaN을 걸러내지 못한다. `HomeStatusBackground`도 `startAnimOffset == bodyPaddingTop`이고 스크롤 값이 그 값과 같으면 `else` 분기에서 `0 / 0f`가 된다.
- 현재는 호출부가 상수(`75.dp - 50.dp = 25.dp`, `32.dp - 20.dp = 12.dp`)이고 세 composable이 모두 `private`이라 도달하지 않는다. 동작 변화 없이 방어만 추가한다. 음수 offset은 애니메이션 구간이 0으로 clamp되어 기존에도 안전했다.
- `BottomBlurLayer`: `maxBottomContainerAnimPx <= 0f`이면 나눗셈을 건너뛰고, 아래쪽에 스크롤할 내용이 남아 있으면(`remainScrollToPx > 0`) 알파 `1f`, 아니면 `0f`로 한다.
- `HomeStatusBackground`: `curState > bodyPaddingTopToPx`를 `>=`로 바꾼다. `else` 분기는 `startAnimOffset < bodyPaddingTop`일 때만 도달하므로 분모가 항상 양수이고, 두 값이 같으면 `1f`가 된다. 일반 경우의 결과는 동일하다.
- 단위 테스트는 추가하지 않는다(private composable이고 현재 입력으로는 새 분기에 도달하지 않음). `:feature:home` 컴파일과 기존 테스트로 회귀를 확인했다.

## Tasks
1. 변경 전 기준 측정: 현재 브랜치 debug 빌드로 Home 스크롤 시나리오의 gfxinfo(jank %, 90/95/99 percentile 프레임 시간)를 3회 기록하고 변경 전 스크린샷(Home 상단/스크롤 중/하단)을 저장한다 (저장소 외부 스크래치 경로).
   - **Verify**: 측정값/스크린샷이 PR 본문 작성에 쓸 수 있도록 정리됨 (수동 확인).
2. ~~Task 2~~ (Dropped: `HeadBlurLayer`의 `drawBehind` 1노드 통합과 반올림 px 선 그리기가 #893(PR #894)에서 이미 완료됨. 코드 재확인만 수행)
3. `BottomBlurLayer`를 `graphicsLayer(ModulateAlpha)` + 정적 `drawBehind`로 변경한다. 알파는 `graphicsLayer` 람다에서만 계산/읽고, `drawBehind`에서 스크롤 상태 읽기를 제거한다.
   - **Verify**: `./gradlew :feature:home:compileDebugKotlin` 성공, `drawBehind` 내부에 `bodyScrollState`/알파 읽기가 없음을 코드 리뷰로 확인.
4. 영향 모듈 단위 테스트와 빌드 확인.
   - **Verify**: `./gradlew :feature:home:test` 및 `./gradlew :app:assembleDebug` 성공.
5. UI 시각 동일성 확인: 변경 후 같은 시나리오/스크린샷을 만들어 Task 1과 비교한다 (하단 버튼 영역 알파 전환 시작/끝, 스크롤이 불가능한 짧은 화면에서 레이어 비표시, 0.5dp 선 두께).
   - **Verify**: 전/후 스크린샷 차이 없음 (Maestro 스크린샷 + 육안).
6. 변경 후 gfxinfo 측정을 Task 1과 동일 조건으로 3회 수행하고 전/후 비교표를 작업 메모(PR 본문 초안)에 기록한다. 개선이 확인되지 않아도 결과를 그대로 기록한다.
   - **Verify**: 전/후 수치 비교표 작성됨.

## Verification
testStrategy: unit+ui

- 단위 테스트: `:feature:home` 테스트 및 `:app:assembleDebug`. draw/graphicsLayer 구성 변경이라 신규 단위 테스트 대상 로직은 거의 없고 기존 테스트로 회귀를 확인한다.
- UI QA(Maestro MCP): Compose draw 변경이므로 Home 스크롤에서 전/후 스크린샷을 비교한다.
- 성능: 일회성 로컬 gfxinfo 측정 (가정 2). 코드로 남기지 않는다.
- 수용 기준 매핑: FR-1 -> Task 3 코드 리뷰, FR-2/FR-3 -> 재확인(완료), NFR-1 -> Task 5, NFR-2 -> Task 1,6.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-03 | Initial draft | requirements.md, 사용자 요청(Open Questions 보수적 기본값 적용) |
| 2026-11-03 | FR-4 제외 사유 기록(Compose 1.12.1이라 `computedStateOf` 불가, 1.13 stable 이후 재검토), `HomeStatusBackground`에 TODO 추가 | Compose 성능 점검, 사용자 요청 |
| 2026-11-03 | `BottomBlurLayer`/`HomeStatusBackground`의 0 나누기(NaN) 방어 추가 (Architecture 섹션 신설) | PR #894 정적 분석 노트(사용자 요청) |
| 2026-11-03 | 기준 브랜치가 origin/epic/issue895(#894 포함)로 변경됨. 현재 코드 재점검 후 FR-2/FR-3 완료로 판단, Task 2 Dropped, Task 3를 BottomBlurLayer 전용으로 수정, #894 관련 가정/질문 제거 | 사용자 결정(코디네이터 전달), 코드 재확인 |
