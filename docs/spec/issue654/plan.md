# 계획: Migration `navigation 3` component (#654)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/654
- 요구사항: docs/spec/issue654/requirements.md
- 생성일: 2026-11-15
- 최종 수정일: 2026-11-21

## 컨텍스트/목표

- 프로젝트의 Navigation 2 사용처 전체를 Navigation 3로 전환하고 Navigation 2.x 의존성을 완전히 제거한다.
- 사용자에게 보이는 이동 동작(전환 애니메이션, 예측 back, ViewModel 스코프)은 그대로 유지한다. UI 변경은 없다.
- 대상: `app`, `feature:home`, `feature:all`, `core:androidx:navigation-compose`, `core:component:usermessage-eventbus`, `build-logic`, 버전 카탈로그.
- 네이밍은 Now in Android(NIA, 로컬 `nowinandroid` 저장소, main, 읽기 전용 참고)를 따른다(D-11). 기능 모듈은 `api`/`impl`로 분리하고(D-12), `impl` 소스 패키지는 `.impl`(D-13), entry 함수는 `Navigator`를 받는다(D-14). 구조는 `Navigator` 단일 클래스이다(D-17).
- 진행 상태(2026-11-21): 구현 Task는 모두 완료되었다. 남은 것은 사용자 수동 테스트(Task 9, NFR-2)이다.

## 요구사항

requirements.md의 FR/NFR과 결정 사항(D-1 ~ D-17)을 따른다. 요약:

- FR-1~4: Navigation 2 사용 코드 전체를 Navigation 3로 전환, stable 버전만 사용(`navigation3-runtime`/`navigation3-ui` 1.2.1, `lifecycle-viewmodel-navigation3` 2.11.0), Navigation 2 완전 제거, 기존 이동 동작(백스택, 전환 애니메이션, 예측 back) 유지.
- NFR-1/2: 기존 자동 테스트가 깨지지 않아야 하며, 이동 동작의 최종 검증은 사용자 수동 테스트로 한다.
- D-7/D-8: 래퍼 모듈 이름 유지, `hilt-navigation-compose`를 `hilt-lifecycle-viewmodel-compose:1.4.0`으로 대체.
- D-11~D-14: NIA 네이밍, `api`/`impl` 분리와 `.impl` 패키지, entry가 `Navigator`를 받음(`home:impl` -> `all:api` 의존).
- D-15: 레거시 Transition typealias 삭제, `goBack` 안전 처리.
- D-17: `NavigationState` 없이 `Navigator`가 back stack을 소유/공개, `PlaneatAppState`가 `backStack`을 소유(`rememberPlaneatAppState`가 `rememberNavBackStack`으로 생성)하고 `PlaneatApp`이 `Navigator`를 생성, `NavDisplay(backStack=...)` 직접 호출, entry provider는 `impl.ui`, Jacoco `navigation` 제외 규칙 제거.

## 아키텍처/설계

### 의존성
- `libs.versions.toml`
  - 제거: `androidxNavigation`, `androidx-navigation-compose`, `androidxHiltNavigationCompose`, `androidx-hilt-navigation-compose`
  - 추가: `androidx-navigation3-runtime`, `androidx-navigation3-ui` (`androidx.navigation3`, 버전 `androidxNavigation3`), `androidx-lifecycle-viewmodel-navigation3` (기존 `androidxLifecycle` 버전 참조), `androidx-hilt-lifecycle-viewmodel-compose` (`androidx.hilt`, 1.4.0)
- `nlab.android.library.feature.impl`과 `usermessage-eventbus`는 `hilt-lifecycle-viewmodel-compose`를 사용한다. `hiltViewModel()` import 경로(`androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`)는 그대로이다.
- `usermessage-eventbus`의 `navigation-compose` 의존은 제거했다.
- `AndroidJacoco.kt`의 `**/navigation/**`, `**/*Args*.*`, `**/*Directions*.*` 제외 규칙은 제거한다(D-17).

### 네이밍 (NIA 규칙 적용)

키는 `<Feature>NavKey`(`NavKey` 구현, `@Serializable`, public), 키 파일은 `<Feature>NavKey.kt`(`:api` 모듈), entry 등록은 `EntryProviderScope<NavKey>.<feature>Entry(...)` 확장 함수이고 파일은 `<Feature>EntryProvider.kt`(`:impl` 모듈, `*EntryProvider`는 파일명이며 클래스가 아니다)이다.

| 기존 이름 | 새 이름 | 비고 |
|-----------|---------|------|
| `HomeRoute` (`internal data object`) | `HomeNavKey` (`public`, `data object`, `@Serializable`, `: NavKey`) | `app`이 시작 키로 직접 참조하므로 public |
| `AllRoute` (`internal data object`) | `AllNavKey` (`public`, `data object`, `@Serializable`, `: NavKey`) | 이동 함수가 public API에 필요 |
| `HomeEntryPointRoute`, `homeEntryPoint` | 삭제 | Navigation 3에 중첩 그래프 개념 없음. `rememberPlaneatAppState`가 `HomeNavKey`를 시작 키로 직접 넘김 |
| `NavGraphBuilder.homeScreen(...)` | `EntryProviderScope<NavKey>.homeEntry(...)` | NIA `forYouEntry` |
| `NavGraphBuilder.allScreen(...)` | `EntryProviderScope<NavKey>.allEntry(...)` | NIA `searchEntry` |
| `NavController.navigateToAll()` | `Navigator.navigateToAll()` (`AllNavKey.kt`) | NIA `Navigator.navigateToTopic` 패턴 |
| `HomeNavigation.kt` | `HomeNavKey.kt`(`:feature:home:api`) + `HomeEntryProvider.kt`(`:feature:home:impl`, 패키지 `.impl.ui`) | |
| `AllNavigation.kt` | `AllNavKey.kt`(`:feature:all:api`) + `AllEntryProvider.kt`(`:feature:all:impl`, 패키지 `.impl.ui`) | |
| `PlaneatAppState.navController` (`NavHostController`) | `PlaneatAppState.backStack: NavBackStack<NavKey>` (`public`) | `rememberPlaneatAppState`가 `rememberNavBackStack(HomeNavKey)`로 만든다. 딥링크 등 `PlaneatApp` 바깥에서 초기 스택이 필요해질 때를 대비해 appState가 소유한다(NIA `NiaAppState.navigationState`에 대응, 단 `NavigationState` 없이 `NavBackStack`) |
| `rememberNavController()` | `rememberNavBackStack(HomeNavKey)` (`rememberPlaneatAppState`에서 호출) | 시작 키 `HomeNavKey` 고정. 별도 `rememberNavigator` 헬퍼는 두지 않는다 |
| (신규) | `Navigator` | `PlaneatApp`이 `remember(appState.backStack) { Navigator(appState.backStack) }`로 만들어 같은 곳의 `NavDisplay`에 쓴다 (NIA `NiaApp`과 같은 위치) |
| `EnterTransitionFactory`, `ExitTransitionFactory` | 삭제 (D-15) | `NavComposeExt.kt`를 제거하고 `PlaneatNavTransitions.kt`의 `AnimatedContentTransitionScope<Scene<*>>` 확장 함수로 직접 합성 |

유지하는 이름: `PlaneatAppState`, `rememberPlaneatAppState`, `PlaneatApp`, `core:androidx:navigation-compose` (패키지는 `com.nlab.reminder.core.androidx.navigation`).

### 상태 구조 (D-17: `Navigator` 단일 클래스)

NIA는 `NavigationState`(탭별 스택 상태)와 `Navigator`(이동 로직)로 나누지만, 이 앱은 단일 back stack이므로 `NavigationState`, 탭 스택(`topLevelStack`/`subStacks`), `currentKey`, `startKey` 프로퍼티, 딥링크 처리(`deepLinkKey`)는 두지 않는다. 딥링크가 생기면 초기 back stack 구성은 `app`에서 처리한다(D-10).

`Navigator` 계약:
- `Navigator(val backStack: NavBackStack<NavKey>)`. `backStack`은 `NavDisplay` 렌더링용으로 공개하며, 이동은 `navigate`/`goBack`으로만 하는 것을 관례로 한다.
- 생성: 사용처가 `rememberNavBackStack(startKey)`로 스택을 만들고 `remember(backStack) { Navigator(backStack) }`로 `Navigator`를 기억한다(`rememberNavigator` 헬퍼는 두지 않는다). 생성자를 `remember` 없이 composition에서 직접 호출하면 상태가 저장되지 않는다. `startKey`는 스택을 처음 만들 때만 쓰인다.
- `navigate(key)`: 이미 스택에 있으면 제거 후 끝에 추가한다(시작 키도 동일하며 스택을 비우지 않는다). 주의: `[Home, All]`에서 `navigate(Home)`은 `[All, Home]`이 되어 back 시 All로 간다. 현재 Home으로 이동하는 호출이 없고 테스트로 문서화했으며, 생기면 정책을 다시 검토한다.
- `goBack()`: 스택 크기가 2 이상이면 마지막 키를 제거한다. 크기가 1 이하이면 아무 동작도 하지 않는다(D-15, 공식 가이드 패턴 적용 및 런타임 크래시 방지). 반환값은 `NavKey?`로, pop한 키이며 무시했으면 `null`이다.

위치: `Navigator`는 래퍼 모듈 `core:androidx:navigation-compose`의 패키지 `com.nlab.reminder.core.androidx.navigation`에 둔다(NIA는 별도 `core:navigation` 모듈이나 신규 모듈은 만들지 않는다, Q2). 이 모듈은 `Navigator` 테스트가 있어 jacoco를 적용한다(PR 리뷰). `NavEntry` 변환(`toEntries`)은 공유 모듈에 두지 않고 `PlaneatApp`이 `NavDisplay`를 직접 호출한다.

### 모듈 구조 (D-12, NIA `api`/`impl` 분리)

NIA 구조: `settings.gradle.kts`에 `include(":feature:foryou:api")`, `include(":feature:foryou:impl")` 식으로 등록한다. `api`에는 `<Feature>NavKey`(public, `@Serializable`)만 있고, `impl`에 화면, ViewModel, `<Feature>EntryProvider.kt`(public entry 확장 함수)와 테스트가 있다. `app`은 모든 `api`와 `impl`을 `implementation`으로 의존하고, feature `impl`은 다른 feature의 `impl`을 의존하지 않고 `api`만 의존한다(예: `foryou:impl` -> `topic:api`).

이 프로젝트 적용(old -> new):

| 기존 | 신규 | 내용 |
|------|------|------|
| `:feature:home` | `:feature:home:api` | `HomeNavKey`(public). 의존: 래퍼 `core:androidx:navigation-compose`(`api`), serialization |
| | `:feature:home:impl` | `HomeScreen`, `HomeEnvironment`/`HomeReduce`/`HomeAction`/`HomeUiState`/`StateSyncFlow`(`internal`), `homeEntry(navigator, ...)`(public, `app`이 호출), 단위 테스트(`HomeReduceKtTest`, `StateSyncedFlowKtTest`, `HomeTestGenerator`), `AndroidManifest.xml`. `home:api`와 `all:api` 의존(D-14) |
| `:feature:all` | `:feature:all:api` | `AllNavKey`(public), `Navigator.navigateToAll()` |
| | `:feature:all:impl` | `AllScreen`(`internal`), `AllScheduleTriggerAtFormatPatterns`/`FakeData`(`internal`), `allEntry(navigator, ...)`(public), 매니페스트. `all:api` 의존 |
| `app`: `projects.feature.home`, `projects.feature.all` | `app`: `projects.feature.home.api`/`.impl`, `projects.feature.all.impl` | `app`은 `HomeNavKey`(시작 키)를 직접 쓰므로 `home:api`가 필요하고, Hilt 그래프와 entry 호출 때문에 `impl`도 필요하다. `all:api`는 직접 쓰지 않아 의존하지 않는다(D-15) |

- 의존 그래프는 비순환이다: `home:impl` -> `all:api` -> 래퍼. `all:*`은 `home:*`을 의존하지 않는다. `feature.api`/`feature.impl` 컨벤션 플러그인은 프로젝트 간 의존을 제한하지 않으므로 `home:impl`의 `build.gradle.kts`에 `implementation(projects.feature.all.api)`만 추가했다.
- 컨벤션 플러그인: `nlab.android.library.feature.api`(`nlab.android.library` + serialization 플러그인 + 래퍼 `api` 의존 + `kotlinx-serialization-json` `implementation`, jacoco/hilt/statekit 미적용)와 `nlab.android.library.feature.impl`(component, di, jacoco, statekit, `hilt-lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`)을 만들고 기존 `nlab.android.library.feature`는 제거했다.
- 패키지/네임스페이스(D-13): `api`는 `com.nlab.reminder.feature.<name>.api.navigation`(namespace `...<name>.api`), `impl`은 `com.nlab.reminder.feature.<name>.impl`(하위 `.ui`; entry provider도 `.impl.ui`, D-17)이고 namespace도 `...<name>.impl`이다.
- jacoco: `nlab.android.library.jacoco`는 `impl` 플러그인이 적용하므로 `impl`에서만 `jacocoTestReport<Variant>` 태스크가 생긴다. `api`는 적용하지 않고, 테스트가 있는 `core:androidx:navigation-compose`는 직접 적용한다(`Navigator` 커버리지 100%). entry provider는 `impl.ui`에 있어 기존 `**/ui/**` 제외 규칙으로 제외된다. CI(`.github/workflows/ci.yml`)는 `./gradlew jacocoTestReportJvm`, `jacocoTestReportRelease`를 모듈 이름 없이 호출하므로 모듈 경로 변경이 필요 없다.
- 테스트: feature에는 `-test` 모듈이 없다. `home:impl`이 기존 `testImplementation(projects.core.component.tagTest)`, `projects.core.dataTest`를 가져간다. `api`는 테스트가 없다.
- 가시성: `impl`의 화면/ViewModel/Environment 등은 `internal`이고, `impl`에서 외부로 공개하는 것은 `homeEntry`/`allEntry`뿐이다. `api`에서는 `NavKey`와 `navigateToAll`만 public이다.

### 화면 구성 (`NavDisplay`)
- `PlaneatAppState`가 `backStack`을 소유하고(`rememberPlaneatAppState`가 생성), `PlaneatApp`이 `Navigator`를 만들고 `entryProvider { homeEntry(...); allEntry(...) }`와 `NavDisplay(backStack = navigator.backStack, entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()), entryProvider = entryProvider, onBack = { navigator.goBack() })`를 직접 사용한다(NIA `NiaApp`처럼 별도 `NavHost` 함수 없이 인라인). 전환 함수는 `PlaneatNavTransitions.kt`에 둔다. decorator는 화면 상태 보존과 ViewModel 스코프(기존 `hiltViewModel()`의 NavBackStackEntry 스코프)를 유지한다.
- entry 시그니처(D-14):
  - `allEntry(navigator: Navigator, enterTransitionTimeInMillis: Int, metadata: Map<String, Any> = emptyMap(), showAppToast: (String) -> Unit)`: `AllScreen`에 `onBackClicked = { navigator.goBack() }`를 전달한다. `metadata`는 전환 spec을 `app`이 정의하므로 유지하고, `showAppToast`와 `enterTransitionTimeInMillis`도 이동이 아니므로 값/람다로 유지한다.
  - `homeEntry(navigator: Navigator)`: `HomeScreen`에 `onAllCategoryClicked = { navigator.navigateToAll() }`를 전달한다. Today/Timetable은 이동 대상 `NavKey`가 아직 없는 `TODO`이므로 `homeEntry` 안의 빈 람다로 처리하고 인자로 노출하지 않는다.
  - `HomeScreen`/`AllScreen`의 시그니처(람다)는 `internal`이며 변경하지 않는다.
  - 단위 테스트: entry는 Compose 연결 코드라 JVM 단위 테스트 대상이 아니다. 이동 로직은 `NavigatorTest`가 검증하고, `navigateToAll()`은 `navigate(AllNavKey)` 한 줄 확장이라 별도 테스트를 만들지 않는다.
- 전환 매핑
  - 기본 전환 None -> `NavDisplay`의 `transitionSpec`/`popTransitionSpec`/`predictivePopTransitionSpec` 기본값을 None으로 지정 (기존 `NavHost` 기본값 동작 유지)
  - Home -> All(`transitionSpec`), All -> Home(`popTransitionSpec`, `predictivePopTransitionSpec`) 전환 -> `allEntry`의 `metadata`로 지정. Navigation 3는 push/pop되는 entry의 metadata를 전환 양쪽에 사용하므로 All의 metadata에 둔다.
- `AllScreen`의 `enterTransitionTimeInMillis` 지연 표시 로직은 변경하지 않는다.
- `AndroidManifest.xml`의 `enableOnBackInvokedCallback`은 유지한다.

## 작업

상태 범례: [완료]는 해당 커밋으로 이미 반영되어 다시 하지 않는다. 커밋 해시는 `git log`로 확인한다. Task 1~12는 `api`/`impl` 분리 전 모듈 구성 기준으로 수행했고, 이후 Task가 현재 구조로 바꿨다.

1. [완료] 버전 카탈로그에 Navigation 3 항목 추가(stable 버전 재확인).
2. [완료] 래퍼 모듈 `core:androidx:navigation-compose`를 Navigation 3 의존(`api` 노출)으로 교체.
3. [완료] `hilt-navigation-compose`를 `hilt-lifecycle-viewmodel-compose`로 대체하고 `usermessage-eventbus`의 `navigation-compose` 의존 제거.
4. [완료] `feature:all`: `AllNavKey`/`allEntry`로 전환.
5. [완료] `feature:home`: `HomeNavKey`/`homeEntry`로 전환.
6. [완료] `app` 전환: `PlaneatAppState`/`PlaneatApp`/`PlaneatNavHost`를 `NavDisplay` 기반으로 교체(이후 Task 31에서 `PlaneatNavHost` 제거)하고 Navigation 2 의존 제거. 구조는 이후 Task 26~28에서 D-17로 정리.
7. [완료] 카탈로그에서 Navigation 2 항목 4개 삭제, 전이 의존 확인.
8. [완료: 2026-11-20 확인, 별도 커밋 없음] 최종 빌드/테스트/의존성 확인.
   - **Verify**: `./gradlew :app:assembleDebug :core:androidx:navigation-compose:test :core:component:usermessage-eventbus:test :feature:home:impl:test :feature:all:impl:test :feature:home:api:assembleDebug :feature:all:api:assembleDebug` 통과, `app`/래퍼/`usermessage-eventbus`/`feature:*:api`/`feature:*:impl` 의 `debugRuntimeClasspath`에서 `androidx.navigation:`이 나오지 않는다.
9. [보류: 사용자 결정(2026-11-20)으로 Maestro 실행 없이 NFR-2 수동 테스트로 대체] 이동 흐름 확인: Home -> All 이동, back, 전환 애니메이션, 예측 back, 구성 변경/프로세스 복원 시 백스택/ViewModel 유지.
10. [완료] `Navigator` 단위 테스트(`NavigatorTest`) 작성: 추가, 중복 없음, 중간 키 top 이동, 시작 키 top 이동, `goBack`, 시작 키에서 `goBack` 무시, 시작 키 이동 후 `goBack`. 이후 구조 변경에 맞춰 갱신.
11. [완료] 이동 로직 구현. 최종 형태는 Task 26(`Navigator` 단일 클래스).
12. [완료: 별도 커밋 없음] 이전 구현 시도의 미커밋 변경을 폐기하고 카탈로그 항목만 대조.
13. [완료] `nlab.android.library.feature.api`/`.impl` 컨벤션 플러그인 추가.
14. [완료] `:feature:all:api` 생성(`AllNavKey`).
15. [완료] `:feature:all:impl` 생성(기존 `:feature:all` 대체, 패키지 `.impl`).
16. [완료] `:feature:home:api` 생성(`HomeNavKey`).
17. [완료] `:feature:home:impl` 생성(기존 `:feature:home` 대체, 패키지 `.impl`).
18. [완료] 기존 `nlab.android.library.feature` 컨벤션 제거.
19. [완료: 별도 커밋 없음] jacoco/테스트 연동 확인: `impl`만 jacoco 태스크를 가지고, 옛 모듈 경로 참조가 없음을 `git grep`으로 확인했다.
20. [완료] `AGENTS.md` 모듈 구조 갱신.
21. [완료] `allEntry`가 `Navigator`를 받도록 변경.
22. [완료] `homeEntry`가 `Navigator`를 받도록 변경하고 `home:impl` -> `all:api` 의존 추가.
23. [완료] 공식 스킬 검토 반영: `NavComposeExt.kt` 제거, 트랜지션 직접 합성, `app`의 `feature.all.api` 의존 제거, `goBack` 안전 처리(D-15).
24. [완료] 과설계 리뷰 1차 반영: `startKey` 초기화 분기 제거(D-16).
25. [완료] 리뷰 후 `NavigationState` 최소형 복원(이후 Task 26에서 다시 폐기).
26. [완료] D-17 구현: `NavigationState`를 폐기하고 `Navigator(val backStack)`/`rememberNavigator`로 합쳤다(`rememberNavigator`는 Task 29에서 제거). `PlaneatAppState`에서 navigation을 제거하고 `PlaneatApp`에서 `Navigator`를 만들도록 했다(이후 Task 29~31에서 현재 형태로 정리). `NavigatorTest`는 `Navigator(backStack)`를 직접 만든다.
27. [완료] D-17 구현: `homeEntry`/`allEntry` 파일을 `impl/ui`로 옮기고(패키지 `.impl.ui`) `AndroidJacoco.kt`의 `**/navigation/**`, `**/*Args*.*`, `**/*Directions*.*` 규칙을 제거했다(같은 커밋. `PlaneatNavHost.kt`를 함께 수정해 중간 상태가 빌드되지 않기 때문).
   - **Verify**: `find . -path '*/src/main/*' \( -path '*/navigation/*' -o -name '*Args*' -o -name '*Directions*' \) -not -path '*/build/*'`의 결과가 `feature/*/api`, `core/androidx/navigation-compose` 파일뿐이다(후자는 이후 jacoco를 적용했고, 제외 규칙이 없어 `Navigator`가 측정된다). `:feature:home:impl:jacocoTestReportRelease` 리포트에 `HomeEntryProviderKt`가 없다(이 확인은 변경 전에도 통과하므로 보조 수단).
28. [완료] `Navigator.toEntries`를 삭제하고 `NavDisplay(backStack = navigator.backStack, entryDecorators = ..., entryProvider = ...)`를 직접 호출하도록 변경(공유 모듈에 재사용 가능한 변환 API를 두지 않기 위해).

29. [완료] D-17 정리: `rememberNavigator`를 삭제하고 `PlaneatApp`이 `rememberNavBackStack(HomeNavKey)`와 `remember(backStack) { Navigator(backStack) }`로 `Navigator`를 직접 만든다. `Navigator` KDoc에서 `rememberNavigator` 안내를 `remember` 사용 안내로 바꾼다.
   - **Verify**: `./gradlew :core:androidx:navigation-compose:test :app:assembleDebug` 통과.

30. [완료] 딥링크 대비(NIA 배치): `PlaneatAppState`가 `val backStack: NavBackStack<NavKey>`를 소유한다. `rememberPlaneatAppState`가 `rememberNavBackStack(HomeNavKey)`로 만들고, `PlaneatApp`이 `remember(appState.backStack) { Navigator(appState.backStack) }`로 `Navigator`를 만들어 `PlaneatNavHost(appState, navigator)`에 넘겼다(Task 31에서 `PlaneatNavHost`를 인라인). (먼저 `Navigator`를 appState가 소유하도록 바꿨다가 사용자 요청으로 `backStack` 소유로 수정)
   - **Verify**: `./gradlew :core:androidx:navigation-compose:test :app:assembleDebug` 통과.

31. [완료] `PlaneatNavHost` 제거: 호출처가 `PlaneatApp` 하나뿐이고 `navigator` 중계만 늘어 NIA처럼 `entryProvider`와 `NavDisplay`를 `PlaneatApp`에 인라인한다. 전환 함수와 상수는 `PlaneatNavHost.kt`를 `git mv`한 `PlaneatNavTransitions.kt`(`internal`)에 둔다.
   - **Verify**: `./gradlew :app:assembleDebug` 통과.

## 검증

testStrategy: unit+ui

- 선택 이유: D-12 분리는 구조 변경이라 자동 검증은 빌드와 기존 단위 테스트의 이동 후 통과로 충분하고, 분리 후 Hilt/entry 연결 회귀는 이동 흐름 확인으로 한다. `Navigator`는 JVM에서 검증 가능한 로직이므로 단위 테스트(`NavigatorTest`)를 추가했다. 전환 애니메이션 품질과 예측 back의 최종 검증은 사용자 수동 테스트(NFR-2)가 담당한다. NFR-1(신규 커버리지 목표 없음)은 기존 테스트 범위에 대한 요구로 유지한다.
- 단위 테스트: 변경한 모듈(`core:androidx:navigation-compose`, `core:component:usermessage-eventbus`, `feature:home:impl`, `feature:all:impl`)로 범위를 제한한다. `feature:*:api`는 로직이 없어 `assembleDebug`로 확인한다. 기존 `HomeReduceKtTest`, `StateSyncedFlowKtTest`는 `home:impl`에서 그대로 통과해야 한다.
- 의존성 검증: Task 7과 Task 8의 `dependencies` 확인으로 Navigation 2가 남아 있지 않음을 증명한다.
- 사용자 수동 테스트 체크리스트(인수 조건): Home -> All 슬라이드/페이드, 복귀 시 반대 방향 전환, 예측 back 제스처 미리보기, 구성 변경(회전)과 프로세스 복원 시 백스택/ViewModel 유지, `All` 화면의 지연 표시 타이밍.

## 확정된 결정 (사용자 승인)

- Q1: `Navigator.navigate(key)`는 이미 스택에 있으면 제거 후 추가한다(NIA 방식). Navigation 2의 연타 시 중복 push가 사라지는 것은 엄밀히는 동작 변경이나 개선이다.
- Q2: `Navigator`는 신규 모듈 없이 래퍼 모듈 `core:androidx:navigation-compose`에 둔다. 단위 테스트를 가볍게 둘 수 있고 D-7과 충돌하지 않는다.
- Q3: feature entry 함수는 NIA처럼 `Navigator`를 받는다(D-14). 이전의 "람다 유지" 결정은 폐기되었다. 이동이 아닌 값(`showAppToast`, `enterTransitionTimeInMillis`, `metadata`)만 인자로 유지한다. 대상 `NavKey`가 아직 없는 `onTodayCategoryClicked`/`onTimetableCategoryClicked`는 PR 리뷰로 `homeEntry` 내부 `TODO`로 옮겼다.
- Q4: 시작 키에서 `goBack()`은 예외 대신 아무 동작도 하지 않는다(D-15). `NavDisplay.onBack` 호출 타이밍에 따른 크래시를 방지한다.
- Q5: `impl` 소스 패키지와 namespace는 `.impl`, `api`는 `.api.navigation`(D-13). 소스/테스트의 `package`/import가 바뀌어 diff가 늘어나는 것을 수용한다.
- 남은 미결정 질문: 없음.

---

## 변경 이력
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-15 | 최초 작성. NIA 기준 네이밍/구조, `api`/`impl` 분리(D-12), `.impl` 패키지와 `Navigator` 인자(D-13/D-14), 공식 스킬 검토(D-15) 반영. Task 1~23 정의 | requirements.md, 사용자 결정, NIA 조사 |
| 2026-11-18 | PR 리뷰 반영(D-16): 과설계 지적으로 구조 단순화, Task 24 추가 | 사용자 결정(리뷰 반영), Opus 리뷰 |
| 2026-11-19 | `NavigationState` 최소형 복원(Task 25) 후 D-17로 다시 폐기(`Navigator`가 back stack 소유/공개, `PlaneatApp`에서 `rememberNavigator`, entry provider `impl.ui` 이동, Jacoco `navigation` 제외 규칙 제거, Task 26/27) | 사용자 결정, Opus 리뷰 |
| 2026-11-20 | `Navigator.toEntries` 제거(Task 28, D-17에 반영). 문서 전체 정리: 대체된 결정의 낡은 문구, 틀린 로컬 경로, 모순된 Q3 배경, 폐기된 계약과 NIA 상세, 완료 Task의 장문 설명 제거. Task 7/8을 완료로 정정(Navigation 2 잔존 없음, 테스트 통과 확인), Task 9는 사용자 결정으로 보류 | 사용자 요청(문서 정리) |
| 2026-11-21 | `rememberNavigator` 제거(Task 29): `PlaneatApp`이 `remember`로 `Navigator`를 직접 만든다. 상태 구조/매핑/위치 문구 갱신 | 사용자 결정 |
| 2026-11-21 | Task 30: 딥링크 대비로 `Navigator`를 `PlaneatAppState`가 소유하도록 변경, 매핑/상태 구조/화면 구성 문구 갱신 | 사용자 결정 |
| 2026-11-21 | Task 30 수정: appState가 `Navigator` 대신 `backStack`을 소유하고 `PlaneatApp`이 `Navigator`를 생성하도록 매핑/상태 구조/화면 구성 문구 갱신 | 사용자 결정 |
| 2026-11-21 | Task 31: `PlaneatNavHost` 인라인(NIA `NiaApp` 방식), 전환 함수는 `PlaneatNavTransitions.kt`로 분리, 매핑/화면 구성 문구 갱신 | 사용자 결정 |
| 2026-11-21 | 문서 정리: 제거된 `PlaneatNavHost`를 가리키던 문구(공유 모듈 위치, Task 26/28, 유지하는 이름) 수정, Task 29~31의 "이 변경의 커밋"을 실제 커밋 해시로 교체, 수정일/진행 상태 날짜 갱신 | 사용자 요청(문서 정리) |
| 2026-11-21 | 커밋 정리(squash)로 구 커밋 해시가 무효가 되어 Task 완료 표기에서 해시를 제거 | 사용자 요청(커밋 정리) |
| 2026-11-21 | PR 리뷰 반영: `Navigator.goBack()`이 `Boolean`을 반환, `homeEntry`가 Today/Timetable `TODO` 콜백을 내부에서 처리, 패키지를 `com.nlab.reminder.core.androidx.navigation`으로 변경 | 사용자 결정(PR 리뷰) |
| 2026-11-21 | PR 리뷰 반영: `Navigator.goBack()`이 popped key(`NavKey?`)를 반환, `core:androidx:navigation-compose`에 jacoco 적용 | 사용자 결정(PR 리뷰) |
