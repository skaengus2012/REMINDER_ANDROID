# 계획: [Refactor] Replace custom Result with Kotlin standard Result (#905)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/905
- 요구사항: docs/spec/issue905/requirements.md
- 작성일: 2026-11-05
- 최종 수정일: 2026-11-05

## Context/Goals
- 프로젝트는 `core:kotlin`의 자체 `Result`(sealed class)와 `catching` 등을 사용한다. 표준 `kotlin.Result`(value class)가 Jacoco에 측정되지 않아 만든 것인데, Jacoco 업데이트로 제약이 사라졌다.
- 목표: Jacoco를 `0.8.16`으로 올리고, 모든 모듈에서 자체 `Result` / `catching`을 제거해 `kotlin.Result` / `runCatching` / `suspendRunCatching`으로 전환한다. 전환 후에도 모든 모듈의 Jacoco 커버리지는 100%를 유지한다.
- 작업 기준 브랜치와 PR base는 `master`.

## Requirements
requirements.md의 FR-1 ~ FR-8, NFR-1 ~ NFR-2를 따른다. 요약:
- FR-1/2: Jacoco `0.8.16` 업데이트, 표준 `Result`가 리포트에 반영되는지 검증
- FR-3: 자체 `Result`/`catching` -> `kotlin.Result`/`runCatching` (suspend 블록은 `suspendRunCatching`)
- FR-4: `flatMap` 호출처 없음 확인으로 종료, 신규 `flatMap` 추가 없음
- FR-5: `core/kotlin`의 `Result.kt` 삭제
- FR-6: 테스트를 `Result.success` / `Result.failure` / `exceptionOrNull()`로 수정, mockk value class `returns` 동작 확인
- FR-7: `core:kotlinx:coroutines`에 `suspendRunCatching` 추가(`CancellationException` 재던짐, 그 외 `Throwable`은 `Result.failure`), `core:local`에 의존성 추가. 해당 모듈은 Jacoco/테스트 비적용 유지
- FR-8: `TagEditTask` SUSPEND 우회, `@ExcludeFromGeneratedTestReport`, Jacoco 안내 주석 등 우회 코드를 NFR-2 달성 기준으로 처리
- NFR-1: `./gradlew test` 통과
- NFR-2: 모든 모듈 인스트럭션/브랜치/라인 100%

## Architecture/Design
- `suspendRunCatching`은 `core/kotlinx/coroutines/src/main/java/com/nlab/reminder/core/kotlinx/coroutines/`(기존 소스 디렉터리가 `java`) 아래 새 파일(`ResultExt.kt`)에 `inline fun <T> suspendRunCatching(block: () -> T): Result<T>` 형태로 둔다. `try { Result.success(block()) } catch (e: CancellationException) { throw e } catch (e: Throwable) { Result.failure(e) }`. android `Log`는 쓰지 않는다. `inline`이라 `block` 안에서 suspend 호출이 가능하다.
- 동작 차이 주의: 기존 `catching`과 마찬가지로 새 방식도 `runCatching`처럼 `Throwable` 전체를 잡되 `CancellationException`만 재던진다(리뷰 반영).
- 모든 main의 `catching` 호출 9곳은 suspend 함수 안이므로 모두 `suspendRunCatching`으로 교체한다. 테스트의 `catching` 1곳(`UserMessageResultExtensionsKtTest`)은 non-suspend 헬퍼이므로 `runCatching`을 쓴다.
- 표준 API 대응: `getOrThrow`, `getOrNull`, `getOrElse`, `map`, `mapCatching`, `onSuccess`, `onFailure`, `isSuccess`, `isFailure`는 이름이 같아 `core.kotlin` import 제거만으로 대부분 이어진다. `Result.Success(x)` / `Result.Failure<T>(t)` 직접 생성은 `Result.success(x)` / `Result.failure<T>(t)`로 바꾼다.
- `TagEditTask.kt`: `core.kotlin.*` 와일드카드 import 제거, `Result.Success`/`Result.Failure` 생성 7곳 교체. SUSPEND 우회(미리 만든 `ret` 반환)와 `@ExcludeFromGeneratedTestReport`는 Task 8에서 Jacoco `0.8.16` 결과를 보고 유지/제거를 정한다(제거는 100%가 확인될 때만).
- `UserMessageResultExtensions.kt`: `Result`/`getOrThrow` import를 표준으로 바꾸고, Jacoco 안내 KDoc과 `@ExcludeFromGeneratedTestReport`는 Task 8에서 처리한다.
- `core:kotlin` 모듈 자체는 `Result.kt` 삭제 외에 변경하지 않는다. `Result.kt` 삭제 후 `core:kotlin`에 남는 소스가 없거나 `projects.core.kotlin` 의존이 불필요해진 모듈이 생기면(예: `core:local`, `core:network`) 사용 여부를 확인하고 정리한다(Task 9).
- 테스트 소스셋/Jacoco는 `core:kotlinx:coroutines`에 추가하지 않는다.

## Tasks
1. Jacoco 버전을 `0.8.16`으로 올린다(`gradle/libs.versions.toml`의 `jacoco`). `configureJacocoToolVersion`은 버전 카탈로그에서 읽으므로 코드 변경 없이 반영되는지 확인하고, 하드코딩이 있으면 함께 수정한다.
   - **Verify**: `./gradlew :core:kotlin:jacocoTestReportJvm` 등 한 모듈의 리포트 태스크가 `0.8.16`으로 정상 실행된다(`./gradlew dependencies` 또는 빌드 로그로 버전 확인). `0.8.16`이 해석되지 않으면 즉시 중단하고 보고한다.
2. `core:kotlinx:coroutines`에 `suspendRunCatching`을 추가한다.
   - **Verify**: `./gradlew :core:kotlinx:coroutines:compileKotlin` 성공
3. `core:local`의 `build.gradle.kts`에 `implementation(projects.core.kotlinx.coroutines)`를 추가하고 `DataStores.kt`(`catching { edit(transform) }`)와 `PreferenceDataSource.kt`를 표준 `Result`/`suspendRunCatching`으로 교체한다.
   - **Verify**: `./gradlew :core:local:compileDebugKotlin` (모듈에 test가 있으면 `:core:local:testDebugUnitTest`)
4. `core:network`(`TrustedTimeDataSource.kt`, `LinkThumbnailDataSource.kt`)의 `catching`을 `suspendRunCatching`으로, `Result` import를 표준으로 교체한다. `core:network`는 `core:kotlinx:coroutines` 의존이 이미 있다.
   - **Verify**: `./gradlew :core:network:test`
5. `core:data`(`CompletedScheduleShownRepository.kt`, `ScheduleRepository.kt`, `TagRepository.kt`)와 `core:data-di`(`AppScopeDataModule.kt`)의 자체 `Result` import를 제거하고 표준으로 전환한다.
   - **Verify**: `./gradlew :core:data:compileKotlin :core:data-di:compileDebugKotlin`
6. `core:data-impl` main(`LocalScheduleRepository.kt` 3곳, `LocalTagRepository.kt` 3곳 `catching` -> `suspendRunCatching`, `CompletedScheduleShownRepositoryImpl.kt`, `OfflineFirstLinkMetadataRepository.kt`, `RemoteFirstTimeSnapshotRepository.kt`)을 전환한다. `flatMap` 호출처가 없음을 `grep`으로 재확인하고 결과를 PR 설명에 남긴다(FR-4).
   - **Verify**: `./gradlew :core:data-impl:compileDebugKotlin` 후 `grep -rn "flatMap" --include=*.kt`에서 `Result.flatMap` 호출이 없음을 확인
7. `core:component:usermessage`(`UserMessageResultExtensions.kt`)와 `core:component:tag`(`TagEditStateMachine.kt`, `TagEditTask.kt`), `feature:home`(`HomeReduce.kt`) main 코드를 전환한다. `TagEditTask.kt`의 `Result.Success`/`Result.Failure` 생성은 `Result.success`/`Result.failure`로 교체한다.
   - **Verify**: `./gradlew :core:component:usermessage:compileKotlin :core:component:tag:compileKotlin :feature:home:compileDebugKotlin` (모듈 변종 이름은 실제 태스크에 맞춘다)
8. 테스트 9개(`TagEditStateMachineTest`, `TagEditTaskKtTest`, `UserMessageResultExtensionsKtTest`, `CompletedScheduleDetailShownRepositoryImplTest`, `LocalScheduleRepositoryTest`, `LocalTagRepositoryTest`, `OfflineFirstLinkMetadataRepositoryTest`, `RemoteFirstTimeSnapshotRepositoryTest`, `HomeReduceKtTest`)를 수정한다. `Result.Success/Failure` -> `Result.success/failure`, `(catching(block) as Result.Failure).throwable` -> `runCatching(block).exceptionOrNull()`, `equalTo` 비교는 유지한다. mockk의 `returns`에 `kotlin.Result`(value class)를 쓰는 지점의 동작을 확인하고 필요하면 mockk 호출 방식을 조정한다(FR-6).
    - 결과(실패 경로 테스트): 표준 `Result.map`이 호출 지점에 인라인되면서 실패 분기가 호출 지점의 브랜치로 계측되므로, 성공 경로만 테스트하면 브랜치 100%(NFR-2)가 깨진다. `TagEditStateMachineTest`의 5개 호출 지점(`startRename`, `startDelete`, `tryUpdateName`, `merge`, `delete`)과 `LocalTagRepositoryTest`의 `save`(`Add`)에 실패 경로 테스트를 추가했다. 이 테스트들을 빼면 `core:component:tag`에서 브랜치 5개, `core:data-impl`에서 1개가 미커버가 됨을 확인했다.
    - 구조: `TagEditStateMachineTest`는 메서드별로 성공·실패를 한 테스트로 묶고 로컬 `verify`로 변형을 돌린다. 이름은 줄 전체가 100자 이내가 되도록 `Then Processing, <결과> or failure`로 줄이고, 부족한 설명은 함수 위 KDoc에 적는다. `LocalTagRepositoryTest`는 로컬 함수 없이 `Add` 성공 테스트 바로 뒤에 실패 테스트를 둔다.
   - **Verify**: `./gradlew :core:component:tag:test :core:component:usermessage:test :core:data-impl:test :feature:home:test`
9. 자체 `Result.kt`를 삭제하고 `grep -rn "core.kotlin.Result\|core.kotlin.catching\|catching" --include=*.kt`로 잔존 사용이 없음을 확인한다. 삭제 후 불필요해진 `projects.core.kotlin` 의존이 있는지 확인하고, 있으면 제거한다(없애도 빌드가 통과하는 경우만).
   - **Verify**: `./gradlew :app:assembleDebug` 성공, grep 결과 0건
10. Jacoco 우회 코드(FR-8)를 처리한다. `TagEditTask`의 SUSPEND 우회와 `UserMessageResultExtensions.kt`의 `@ExcludeFromGeneratedTestReport`/KDoc 등을 Jacoco `0.8.16` 리포트 결과에 맞춰 유지/제거/수정하고, 표준 `Result`가 리포트에 반영되는지(FR-2) 확인한다. 100%가 확인되지 않은 우회는 제거하지 않는다.
    - **Verify**: `./gradlew jacocoTestReportJvm jacocoTestReportRelease` 후 대상 모듈 리포트에서 해당 클래스가 측정되고 100%
    - 결과(`CompletedScheduleShownRepositoryImpl`): `@ExcludeFromGeneratedTestReport`와 안내 주석을 제거한다. 미커버 브랜치의 원인은 continuation 상태 머신이 아니라 `isShown`을 박싱하는 Boolean 분기(`true`/`false`)였고, 테스트가 `genBoolean()` 랜덤 값 하나만 넘겨 한쪽만 커버되던 것이다. 테스트에서 `true`/`false`를 모두 호출하도록 고쳐 우회 없이 100%를 확인했다. `delay` 기반 suspend 케이스 테스트는 커버리지에 영향이 없어 삭제한다. 파라미터명은 `getAsStreamFunc`/`setShownFunc`로 정리한다.
    - mockk은 `suspend (Boolean) -> Result<Unit>` 람다를 스텁할 수 없으므로(`Function2.invoke`가 `Object`로 지워짐) 직접 만든 fake로 검증한다.
11. 전체 테스트와 전체 Jacoco 리포트를 실행해 NFR-1, NFR-2를 확인한다. 100%가 아닌 모듈이 있으면 원인(value class 계측 문제, 테스트 부족)을 파악해 수정하고 Task 10으로 돌아간다.
    - **Verify**: `./gradlew test`와 `./gradlew jacocoTestReportJvm jacocoTestReportRelease`, 모든 모듈 인스트럭션/브랜치/라인 100%

## Verification
testStrategy: unit

- 변경은 Compose/레이아웃 UI를 건드리지 않는 리팩터링이므로 Maestro UI QA는 하지 않는다. 변경 모듈 범위의 단위 테스트를 기본으로 하고, 최종 확인은 전체 실행으로 한다.
- 태스크별 확인은 각 Task의 Verify를 따른다. 모듈별 테스트 실행 중 실패가 나면 해당 Task 안에서 고친다.
- Jacoco 규칙(NFR-2): 모든 모듈이 인스트럭션, 브랜치, 라인 100%여야 하며 다음 명령으로 확인한다.
  ```bash
  ./gradlew jacocoTestReportJvm jacocoTestReportRelease
  ```
- 최종 확인: `./gradlew test` 통과, 위 Jacoco 명령 통과, `./gradlew :app:assembleDebug` 성공, 자체 `Result`/`catching` 잔존 사용 0건.
- 위험 요소(검증으로 확인): (a) `0.8.16` 해석 가능 여부와 value class 측정 개선 여부, (b) mockk 1.14.11에서 `returns Result.success(...)` 동작, (c) `catching`의 `Throwable` -> `suspendRunCatching`의 `CancellationException` 재던짐에 따른 테스트 영향.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-05 | Initial draft | requirements.md |
| 2026-11-05 | 리뷰 반영: `suspendRunCatching`이 `Throwable`을 잡도록 수정, 파일명을 `ResultExt.kt`로 변경, `CompletedScheduleShownRepositoryImpl`의 Jacoco 우회 제거(Task 10 결과 기록) | PR 907 리뷰 |
| 2026-11-08 | 리뷰 반영: Task 8에 실패 경로 테스트를 추가한 이유(표준 `Result.map` 인라인으로 생긴 실패 분기, NFR-2)와 테스트 구조(성공·실패 병합, 로컬 `verify`, 이름·KDoc 규칙)를 기록 | PR 907 리뷰 |
