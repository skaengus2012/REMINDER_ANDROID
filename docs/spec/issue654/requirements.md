# 요구사항 분석: Migration `navigation 3` component (#654)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/654
- 상위 이슈: 없음
- 라벨: Feature
- 생성일: 2025-12-07
- 최종 수정일: 2026-11-22
- 분석 기준점: 2025-12-07T14:15:15Z (댓글이 없으므로 이슈 `updatedAt`. 이후 변경된 사항은 이슈가 아니라 사용자 답변이며 변경 이력에 기록)

## 배경 및 문제

- 이슈 본문은 Android 공식 Navigation 3 가이드 링크(https://developer.android.com/guide/navigation/navigation-3?hl=ko) 한 줄뿐이다.
- 이슈 제목은 "Migration `navigation 3` component"이다.
- 댓글과 상위 이슈는 없다. 세부 범위와 결정 사항은 사용자 답변(2026-11-15)으로 보완했다.

## 목표

- 프로젝트의 Navigation 2 사용처 전체를 Navigation 3로 마이그레이션하고, 사용자에게 보이는 이동 동작은 그대로 유지한다.

## 범위

### 포함 범위
- Navigation 2를 사용하는 모든 모듈의 Navigation 3 전환 (사용자 답변 1)
  - `app` (`PlaneatAppState`, `PlaneatApp`, 전환 함수 파일, `app/build.gradle.kts`)
  - `feature:home`, `feature:all` (D-12로 `api`/`impl` 분리)
  - `core:androidx:navigation-compose` 래퍼 모듈
  - `core:component:usermessage-eventbus` (`navigation-compose`, `hilt-navigation-compose` 의존)
  - Navigation 의존성을 주입하는 빌드 로직 (`build-logic`의 feature 컨벤션 플러그인)
  - 버전 카탈로그 `gradle/libs.versions.toml`
  - Navigation Component용 Jacoco 제외 규칙 정리 (`build-logic`의 `AndroidJacoco.kt`, D-17)
- 기능 모듈의 `api`/`impl` 분리 (D-12): `feature:home`, `feature:all`을 각각 `api`/`impl`로 나누고 `settings.gradle.kts`, `app`, `build-logic` feature 컨벤션 플러그인을 맞춘다. FR/NFR의 의미는 바꾸지 않는다.
- Navigation 2.x 의존성의 완전 제거 (사용자 답변 4)

### 제외 범위
- 하위 이슈 분할 및 에픽 브랜치: 사용하지 않고 일반 단일 작업으로 진행 (사용자 답변 9)
- 신규 테스트 커버리지 목표 설정: 없음 (사용자 수동 검증으로 대체, 사용자 답변 8)

## 결정 사항

| 번호 | 결정 | 출처 |
|------|------|------|
| D-1 | 마이그레이션 범위는 Navigation 2 사용처 전체이다. | 사용자 답변 1 |
| D-2 | stable 버전만 허용한다. alpha/beta/rc는 사용하지 않는다. | 사용자 답변 3 |
| D-3 | Navigation 2.x 의존성은 이번 이슈에서 완전히 제거하며 병행하지 않는다. | 사용자 답변 4 |
| D-4 | 기존 이동 동작(백스택, 전환 애니메이션, 예측 back)은 동일하게 유지한다. 탭 상태 보존과 딥링크는 현재 코드에 없으므로 향후 대비 문구이다(D-10). | 사용자 답변 6 |
| D-5 | 하위 이슈 분할이나 에픽 없이 일반 단일 작업으로 진행한다. | 사용자 답변 9 |
| D-6 | 테스트는 사용자가 직접 수동 테스트한다. 자동 테스트는 기존 테스트가 깨지지 않도록 수정하는 수준으로 한정한다. 단, 새로 만든 이동 로직(`Navigator`)은 단위 테스트를 작성한다(D-11). | 사용자 답변 8 |
| D-7 | 래퍼 모듈 `core:androidx:navigation-compose`는 이름을 바꾸지 않고 유지하며, 내용만 Navigation 3로 교체한다. | 사용자 승인 (Gate 1) |
| D-8 | `hilt-navigation-compose`는 `hilt-lifecycle-viewmodel-compose:1.4.0`(POM 확인: navigation-compose 의존 없음)으로 대체하고 Navigation 2를 완전히 제거한다. 다른 라이브러리가 `androidx.navigation:navigation-compose`를 전이 의존으로 끌어오지 않는지 `dependencies`로 검증한다. | 사용자 승인 (Gate 1) |
| D-9 | UI 변경은 없다(내부 교체만). 기존 전환 애니메이션, 예측 back, ViewModel 스코프 동작을 유지하며 시안은 필요 없다. | 사용자 승인 (Gate 1) |
| D-10 | 딥링크와 탭 상태 보존은 현재 코드에 존재하지 않으며 향후 대비 문구로만 취급한다. | 사용자 승인 (Gate 1) |
| D-11 | 네이밍(`*NavKey`, `*Entry`, `Navigator`)은 Now in Android(NIA)를 따르되, 이 앱에 없는 탭 스택, 딥링크 등은 채택하지 않는다. 구조는 D-17로 확정되었다. 새로 추가한 `Navigator` 로직은 단위 테스트를 작성한다. | 사용자 수락 (NIA 범위 지정) |
| D-12 | 기능 모듈을 NIA처럼 `api`/`impl`로 분리한다: `feature:home` -> `feature:home:api` + `feature:home:impl`, `feature:all` -> `feature:all:api` + `feature:all:impl`. `api`에는 `NavKey`(와 이동 확장 함수)만, `impl`에는 화면, ViewModel, entry 함수, 테스트를 둔다. `app`은 `api`와 `impl`을 모두 의존한다. 사용자는 비용(모듈 4개 추가, `build-logic`/`settings`/jacoco 변경, 큰 diff)을 안내받고 수락했다. | 사용자 결정 (2026-11-15) |
| D-13 | `impl` 모듈의 소스 패키지와 namespace는 NIA처럼 `.impl`로 바꾼다(예: `com.nlab.reminder.feature.home.impl...`). `api`는 `.api.navigation`(namespace `.api`). | 사용자 승인 (2026-11-15) |
| D-14 | feature entry 함수는 NIA처럼 `Navigator`를 받아 직접 이동한다(`navigateToAll()`, `goBack()`). `feature:home:impl`은 `feature:all:api`에 의존하고, `feature:all:api`가 `Navigator.navigateToAll()` 확장을 제공한다. 이동이 아닌 값(`showAppToast`, `enterTransitionTimeInMillis`, `metadata`)만 인자로 받는다. 대상 `NavKey`가 아직 없는 Today/Timetable 콜백은 `homeEntry` 안에서 `TODO`로 처리하고 인자로 노출하지 않는다. | 사용자 승인 (2026-11-15) |
| D-15 | 공식 Navigation 3 스킬 검토 결과 불필요한 레거시 Transition typealias(`NavComposeExt.kt`)를 삭제하고 `app`의 전환 함수(`PlaneatNavTransitions.kt`)로 직접 합성하며, `app/build.gradle.kts`의 미사용 `feature.all.api` 의존성을 제거한다. `Navigator.goBack()`은 크래시 방지를 위해 예외 대신 스택 크기가 2 이상일 때만 pop한다. | 사용자 요청 (2026-11-15) |
| D-16 | (D-17로 대체) PR 리뷰의 과설계 지적에 따라 NIA의 탭 스택, 미사용 `startKey` 분기, `currentKey`를 제거하고 단일 back stack으로 단순화했다. 이후 `NavigationState`를 폐기하는 D-17이 구조를 대체했다. | 사용자 결정 (2026-11-18, 2026-11-19) |
| D-17 | 앱이 단일 탭 구조라 `NavigationState` 없이 `Navigator`만 둔다. `Navigator(private val backStack: NavBackStack<NavKey>)`가 back stack에 대한 이동 규칙을 가지며 back stack을 노출하지 않는다(feature가 규칙을 우회하지 못하도록 `private`, 렌더링은 `PlaneatAppState.backStack`을 쓴다), `navigate`(이미 있는 키는 제거 후 top에 추가, 시작 키도 동일하며 스택을 비우지 않음)와 `goBack`(스택 크기 1이면 무시하고 `null`, 아니면 pop한 키를 `NavKey?`로 반환)을 제공한다. `rememberNavigator` 같은 헬퍼는 두지 않고, 사용처가 `rememberNavBackStack(startKey)`와 `remember`로 `Navigator`를 만든다. 공유 모듈에 `NavEntry` 변환 API(`toEntries`)는 두지 않고, `PlaneatApp`이 `NavDisplay(backStack = appState.backStack, entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()), entryProvider = ...)`를 직접 호출한다. `PlaneatAppState`가 `backStack: NavBackStack<NavKey>`를 `public`으로 보유하고(`rememberPlaneatAppState`가 `rememberNavBackStack(HomeNavKey)`로 만든다), `PlaneatApp`이 `remember(appState.backStack) { Navigator(appState.backStack) }`로 `Navigator`를 만들어 같은 곳의 `NavDisplay`에 쓴다(NIA의 `NiaAppState.navigationState` / `NiaApp`의 `Navigator` 생성과 `NavDisplay` 인라인과 같은 배치). 딥링크 등 `PlaneatApp` 바깥에서 초기 스택이 필요해질 때를 대비해 back stack은 appState가 소유한다. `core:androidx:navigation-compose`는 `Navigator` 테스트가 있어 jacoco를 적용하고, 패키지는 `compose` 하위 없이 `com.nlab.reminder.core.androidx.navigation`을 쓴다(`compose` 하위 패키지는 Jacoco 제외 규칙 `**/compose/**`에 걸려 측정되지 않으므로 상위 패키지에 둔다. `**/navigation/**` 규칙은 제거했으므로 새 패키지는 측정된다). `homeEntry`/`allEntry`는 `feature/*/impl`의 `.impl.ui`로 옮겨 기존 `**/ui/**` 제외 규칙을 따르게 한다(`ui` 패키지에는 테스트 대상 로직을 두지 않는다). Navigation Component용 Jacoco 제외 규칙 `**/navigation/**`, `**/*Args*.*`, `**/*Directions*.*`는 의미가 없어 제거한다. 딥링크는 범위에 포함하지 않으며, 생기면 초기 back stack 구성은 `app`에서 처리한다. | 사용자 결정 (2026-11-19, 2026-11-20 `toEntries` 제거 반영) |

## 조사 결과

### 목표 버전 (사용자 답변 3 관련)
2026-11-15 기준으로 Google Maven 메타데이터(`dl.google.com/dl/android/maven2`)를 조회한 결과이다. 구현 시점에 `libs.versions.toml`에 1.2.1/2.11.0으로 반영했다.

| 아티팩트 | 최신 stable | 비고 |
|----------|-------------|------|
| `androidx.navigation3:navigation3-runtime` | 1.2.1 | 최신 전체 버전은 1.3.0-alpha04 (사용 불가) |
| `androidx.navigation3:navigation3-ui` | 1.2.1 | 동일 |
| `androidx.lifecycle:lifecycle-viewmodel-navigation3` | 2.11.0 | 프로젝트의 `androidxLifecycle = "2.11.0"`과 동일 버전 (최신 전체 버전은 2.12.0-alpha05, 사용 불가) |

### 전환 전 Navigation 2 사용 현황 (코드 확인)
- `PlaneatNavHost`가 `NavHost`로 `HomeEntryPointRoute` 내부 그래프(`navigation<HomeEntryPointRoute>`) 아래에 `HomeRoute`, `AllRoute` 두 화면을 구성했다. 이동은 Home에서 All로의 `navigateToAll()`과 All의 `popBackStack()` 뿐이다.
- 전환 애니메이션: 기본값은 모두 `None`이고, Home에서 All 이동 시 슬라이드(300ms)와 Home 페이드, 복귀 시 반대 방향 슬라이드가 지정되어 있다. 예측 back 전환(`predictivePopEnterTransition`, `predictivePopExitTransition`)도 지정되어 있고 `AndroidManifest.xml`에 `android:enableOnBackInvokedCallback="true"`가 설정되어 있다.
- 딥링크: 코드에 `navDeepLink`는 없고, 매니페스트 `intent-filter`는 MAIN/LAUNCHER 하나뿐이다. 탭(하단 내비게이션) 구조도 없다. 내비게이션 대상은 Home, All 두 화면이다.
- 라우트는 `@Serializable` 객체(`HomeRoute`, `HomeEntryPointRoute`, `AllRoute`)였다.
- `core:androidx:navigation-compose`는 `EnterTransitionFactory`, `ExitTransitionFactory` typealias(`NavBackStackEntry` 기반) 두 개만 제공했고, 이 모듈은 `build-logic`의 feature 컨벤션을 통해 모든 feature 모듈에 `implementation`으로 주입된다.
- `hiltViewModel()`은 `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel` 패키지로 `HomeScreen`, `UserMessageAggregateHandler`에서 사용한다.
- 전환 전 Navigation 관련 자동 테스트는 없었다 (`app/src/androidTest`에는 데이터 삽입용 매크로만 존재).

### hilt-navigation-compose 대체 (D-8 근거)
- `hilt-navigation-compose:1.4.0`의 POM은 `hilt-lifecycle-viewmodel-compose:1.4.0`과 `navigation-compose:2.9.0`에 의존한다. 코드에서 쓰는 `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`은 `hilt-lifecycle-viewmodel-compose`에 있으므로, D-3(Navigation 2 완전 제거)을 지키려고 `hilt-lifecycle-viewmodel-compose`로 대체했다. `hiltViewModel()` import 경로는 그대로 유지된다.
- Navigation 3에서 화면(NavEntry) 단위로 ViewModel을 스코프하려면 `lifecycle-viewmodel-navigation3`의 ViewModelStore 데코레이터를 `NavDisplay`의 `entryDecorators`에 추가해야 한다(D-17에 반영).

### 래퍼 모듈 `core:androidx:navigation-compose` 처리 (D-7 근거)
- 기존 코드는 Navigation 2 타입(`NavBackStackEntry`)에 묶인 typealias 두 개뿐이어서 Navigation 3에서 재사용할 수 없었다. Navigation 3의 전환은 `NavDisplay`와 `NavEntry` 메타데이터(`transitionSpec`, `popTransitionSpec`, `predictivePopTransitionSpec`)로 지정하고, `AnimatedContentTransitionScope`의 대상 타입은 `Scene` 계열이다.
- 모듈을 유지하고 내용만 교체했다. 이유는 (1) feature 컨벤션이 이 모듈을 전제로 하고 있어 변경 범위가 작고, (2) Navigation 의존성 버전과 공용 타입을 한 곳에서 관리할 수 있으며, (3) `core:androidx` 아래에 AndroidX 래퍼를 두는 기존 구조와 일치하기 때문이다. 현재 이 모듈은 `navigation3-runtime`, `navigation3-ui`, `lifecycle-viewmodel-navigation3`을 `api`로 노출하고 `Navigator`를 제공한다.

### UI 변경이 생길 수 있는 부분 (사용자 질문 7에 대한 조사)
기본 입장은 UI 변경 없음(내부 교체만)이다. 다만 라이브러리가 바뀌므로 다음 항목에서 눈에 보이는 차이가 생길 수 있다.

1. 화면 전환 애니메이션: 기본 전환, 슬라이드/페이드 spec의 지정 방식이 `NavDisplay` 기준으로 바뀐다.
2. 예측 back 제스처: 제스처 중 미리보기 애니메이션 동작이 `predictivePopTransitionSpec` 기반으로 바뀐다.
3. 화면 상태 보존: 백스택에서 화면이 유지되는 방식(`SaveableStateHolder` 데코레이터)과 ViewModel 스코프가 바뀔 수 있다.
4. 구성 변경/프로세스 복원: 백스택 저장 방식이 바뀐다 (`NavKey`, `rememberNavBackStack`).
5. 화면 전환 중 화면이 겹치는 구간의 렌더링 순서와 `All` 화면의 `enterTransitionTimeInMillis` 기반 콘텐츠 지연 표시 타이밍.

이 이슈에서는 위 항목이 기존과 동일해야 하며, 의도적인 시각 변경은 포함하지 않는다.

## 기능 요구사항

| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `app`, `feature:home`, `feature:all`, `core:androidx:navigation-compose` 래퍼, `core:component:usermessage-eventbus` 등 Navigation 2를 사용하는 모든 코드를 Navigation 3 기반으로 전환한다. | 제목 / 본문(가이드 링크) / 사용자 답변 1 |
| FR-2 | Navigation 3 관련 의존성은 stable 버전만 사용한다. 목표 버전은 `navigation3-runtime`, `navigation3-ui` 1.2.1과 `lifecycle-viewmodel-navigation3` 2.11.0이다. | 사용자 답변 3 (버전은 Google Maven 조사 결과) |
| FR-3 | Navigation 2.x 의존성(`androidx-navigation-compose` 및 이를 끌어오는 전이 의존 포함)을 이번 이슈에서 완전히 제거하고, Navigation 2와 3를 병행하지 않는다. | 사용자 답변 4 |
| FR-4 | 기존 이동 동작을 동일하게 유지한다: 백스택, 전환 애니메이션(Home에서 All 슬라이드 및 페이드, 복귀 시 반대 방향, 예측 back 전환). 탭 상태 보존과 딥링크는 향후 대비 문구이다(D-10). | 사용자 답변 6 |

## 비기능 요구사항

| ID | 요구사항 | 출처 |
|----|----------|------|
| NFR-1 | 기존 자동 테스트가 깨지지 않도록 필요한 수정만 한다. 신규 커버리지 목표는 두지 않는다(새 `Navigator` 로직의 단위 테스트는 D-11로 추가). | 사용자 답변 8 |
| NFR-2 | 이동 동작 검증은 사용자가 직접 수동 테스트로 수행한다. | 사용자 답변 8 |

## 인수 조건

- [ ] Navigation 2 사용처 전체(`app`, `feature:home`, `feature:all`, 래퍼 모듈, `usermessage-eventbus`)가 Navigation 3 기반으로 동작한다. (코드 전환과 빌드는 끝났고, 동작은 사용자 수동 테스트로 확인한다.)
- [x] `libs.versions.toml` 및 모든 `build.gradle.kts`에서 Navigation 2.x 의존성이 제거되고, 전이 의존으로도 남아 있지 않다. (2026-11-20 `dependencies`로 확인)
- [x] Navigation 3 관련 버전이 모두 stable이다.
- [ ] 기존 이동 동작(백스택, 전환 애니메이션, 예측 back, ViewModel 스코프)이 기존과 동일함을 사용자가 수동 테스트로 확인한다.
- [x] 기존 자동 테스트가 통과하고 `:app:assembleDebug`가 성공한다. (2026-11-20 확인)

## 영향 범위

코드에서 확인된 Navigation 2 사용처와 변경 위치이다.

- 버전 카탈로그: `gradle/libs.versions.toml` (Navigation 2 항목 삭제, Navigation 3 항목 추가)
- 래퍼 모듈: `core/androidx/navigation-compose` (`Navigator`, `NavigatorTest`, `build.gradle.kts`의 namespace와 jacoco 적용)
- 모듈 등록: `settings.gradle.kts` (feature `api`/`impl` 4개 모듈)
- 빌드 로직: `build-logic/convention/src/main/kotlin/` 아래 `nlab.android.library.feature.api.gradle.kts`, `nlab.android.library.feature.impl.gradle.kts`(기존 `nlab.android.library.feature` 대체), `AndroidJacoco.kt`(`navigation` 제외 규칙 제거), `AndroidFeature.kt`(삭제, 각 컨벤션 스크립트에 인라인)
- 앱 모듈: `app/build.gradle.kts`, `PlaneatAppState.kt`(`NavHostController`를 `backStack`으로 교체), `PlaneatApp.kt`(`Navigator` 생성, `entryProvider`, `NavDisplay`), `PlaneatNavTransitions.kt`(전환 함수; 이전 `PlaneatNavHost.kt`), `app/src/main/AndroidManifest.xml`(`enableOnBackInvokedCallback` 유지)
- 기능 모듈: `feature/home/{api,impl}`, `feature/all/{api,impl}` (`api`는 NavKey와 이동 확장, `impl`은 화면과 entry, D-12). 이 두 모듈을 의존하는 모듈은 `app`뿐이다.
- 기타: `core/component/usermessage-eventbus/build.gradle.kts`(`navigation-compose` 의존 제거), `UserMessageAggregateHandler.kt`와 `HomeScreen.kt`(`hiltViewModel()` import 경로가 그대로라 코드 변경 없음)

## 충돌 및 참고사항

- 본문이 링크 한 줄이고 댓글이 없어 본문과 댓글 간 충돌은 없다.
- 사용자 답변 6의 "딥링크, 탭 상태 보존"은 현재 코드에 대응되는 기능이 없다. 이는 D-10에서 향후 대비 문구로 확정되었다.

---

## 변경 이력

| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-15 | 최초 작성. 사용자 답변 1~9 반영(결정 사항, 조사 결과, FR 4개, NFR 2개), Gate 1 승인 결정 D-7~D-10 추가 | 본문, 사용자 답변, 코드 및 Google Maven 조사 |
| 2026-11-15 | D-11(NIA 네이밍/구조), D-12(`api`/`impl` 분리), D-13(`.impl` 패키지), D-14(entry가 `Navigator`를 받음), D-15(공식 스킬 검토 반영) 추가 (FR/NFR 의미 변경 없음) | 사용자 결정/승인, NIA 및 공식 스킬 조사 |
| 2026-11-18 | D-16 추가: PR 리뷰 과설계 지적 반영 (FR/NFR 의미 변경 없음) | 사용자 결정(리뷰 반영) |
| 2026-11-19 | D-17 추가: `NavigationState` 폐기, `Navigator`가 back stack 소유/공개, `PlaneatApp`에서 `rememberNavigator`, entry provider `impl.ui` 이동, Jacoco `navigation` 제외 규칙 제거. 포함 범위와 영향 범위 갱신 | 사용자 결정 |
| 2026-11-20 | D-17에 `Navigator.toEntries` 제거(`PlaneatNavHost`에서 `NavDisplay(backStack=...)`와 decorator 직접 구성)를 반영. 문서 전체 정리: 대체된 결정(D-11/D-15/D-16)의 낡은 문구, 해소된 추천안/대안, 오래된 영향 범위 경로 제거, 인수 조건 확인 상태 갱신 | 사용자 요청(문서 정리) |
| 2026-11-21 | D-17에서 `rememberNavigator` 제거: `PlaneatApp`이 `rememberNavBackStack`과 `remember`로 `Navigator`를 직접 만든다 (FR/NFR 의미 변경 없음) | 사용자 결정 |
| 2026-11-21 | 딥링크 대비로 `Navigator`를 `PlaneatAppState`가 소유하도록 변경(D-17 갱신): `rememberPlaneatAppState`가 `Navigator`를 만들고 `PlaneatNavHost`는 `appState.navigator`를 사용 (FR/NFR 의미 변경 없음) | 사용자 결정 |
| 2026-11-21 | `PlaneatAppState`가 `Navigator`가 아니라 `backStack`을 소유하도록 수정(NIA 배치): `Navigator`는 `PlaneatApp`이 `remember`로 만들어 `PlaneatNavHost`에 넘긴다 (FR/NFR 의미 변경 없음) | 사용자 결정 |
| 2026-11-21 | `PlaneatNavHost`를 제거하고 `NavDisplay`/`entryProvider`를 `PlaneatApp`에 인라인(NIA `NiaApp` 방식). 전환 함수는 `PlaneatNavTransitions.kt`로 분리 (FR/NFR 의미 변경 없음) | 사용자 결정 |
| 2026-11-21 | 문서 정리: 현재 코드와 대조해 낡은 내용이 없음을 확인하고 최종 수정일만 갱신 (FR/NFR 의미 변경 없음) | 사용자 요청(문서 정리) |
| 2026-11-21 | PR 리뷰 반영: 패키지를 `com.nlab.reminder.core.androidx.navigation`으로 변경, `homeEntry`가 Today/Timetable `TODO` 콜백을 내부에서 처리해 인자에서 제거(D-14, D-17 갱신) (FR/NFR 의미 변경 없음) | 사용자 결정(PR 리뷰) |
| 2026-11-21 | PR 리뷰 반영: `core:androidx:navigation-compose`에 jacoco 적용(D-17 갱신), `Navigator.goBack()` 반환 타입을 `NavKey?`로 정리 (FR/NFR 의미 변경 없음) | 사용자 결정(PR 리뷰) |
| 2026-11-21 | PR 리뷰 반영: `Navigator.backStack`을 `private`으로 변경하고 `NavDisplay`는 `appState.backStack`을 사용(D-17 갱신) (FR/NFR 의미 변경 없음) | 사용자 결정(PR 리뷰) |
| 2026-11-22 | 문서 정리: 패키지 이동 이유를 `**/compose/**` 제외 규칙 기준으로 정정, D-17에 `goBack` 반환형 추가, 영향 범위(래퍼/모듈 등록/빌드 로직/기타) 보완 (FR/NFR 의미 변경 없음) | 사용자 요청(문서 재검토)
