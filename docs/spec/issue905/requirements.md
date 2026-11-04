# 요구사항 분석: [Refactor] Replace custom Result with Kotlin standard Result (#905)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/905
- 부모 이슈: 없음
- 라벨: Refactor
- 생성일: 2026-11-04
- 최종 수정일: 2026-11-05
- 분석 기준점: 2026-11-04T14:03:17Z
- 코드 기준: `master` (HEAD `9a2c0323`), PR base도 `master`

## 배경 및 문제
- 프로젝트는 `core/kotlin`의 `Result.kt`에 자체 `Result`(data class를 가진 sealed class)와 `catching`, `map`, `flatMap`, `getOrNull` 등의 확장 함수를 두고 있다.
- Kotlin 표준 `kotlin.Result`는 value class라서 Jacoco가 커버리지를 측정하지 못했고, 그래서 자체 클래스를 만들었다.
- Jacoco 업데이트로 이 제약이 사라져 표준 `Result`로 전환할 수 있게 되었다.
- 현재 사용량은 `core.kotlin.Result` import 약 20곳, `catching` 호출 약 16곳이다.
- `libs.versions.toml`은 현재 Jacoco `0.8.14`를 선언한다.
- 이슈에 댓글은 없고, 부모 이슈도 없다. (이슈 본문은 작성 시점 이후 변경이 없다.)

## 목표
- 자체 `Result` / `catching`을 제거하고 모든 모듈에서 `kotlin.Result` / `runCatching`(`suspend` 블록은 `suspendRunCatching`)을 사용한다.
- 전환 후에도 모든 모듈의 Jacoco 커버리지가 100%로 유지됨을 확인한다.

## 범위
### 포함
- Jacoco를 `0.8.16`으로 업데이트(`libs.versions.toml`, `configureJacocoToolVersion`)하고 표준 `Result`가 커버리지 리포트에 제대로 반영되는지 확인
- 모든 모듈의 자체 `Result` / `catching` 사용처를 `kotlin.Result` / `runCatching`으로 교체
- `flatMap` 사용처 확인 (사용처 없음으로 확인됨, FR-4)
- `suspend` 블록용 `suspendRunCatching` 추가 (FR-7)와 사용 모듈의 `core:kotlinx:coroutines` 의존성 추가
- 자체 `Result.kt` 삭제와 관련 테스트 수정
- 모든 모듈 커버리지 100% 유지를 위한 Jacoco 우회 코드(`TagEditTask` 등) 처리 (FR-8)
- 전체 테스트와 Jacoco 리포트 실행

### 제외
- 에러 처리 구조 개선, `Flow<Result<...>>` 설계 변경 등 이슈 밖의 `Result` 사용 방식 개선 (사용자 결정)
- 신규 `flatMap` 확장 함수 추가 (사용자 결정)

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | Jacoco 버전을 `0.8.16`으로 올린다 (`libs.versions.toml`과 `configureJacocoToolVersion`). | 본문 |
| FR-2 | 표준 `kotlin.Result`가 Jacoco 커버리지 리포트에 올바르게 반영되는지 검증한다. | 본문 |
| FR-3 | 모든 모듈에서 자체 `Result` / `catching` 사용을 `kotlin.Result` / `runCatching`으로 교체한다. 단 `suspend` 블록 지점은 FR-7의 `suspendRunCatching`을 쓴다. | 본문, 사용자 결정 |
| FR-4 | `flatMap` 사용처를 확인한다. 현재 코드에 `Result.flatMap`의 호출처가 없음을 확인했으므로 "사용처 없음 확인"으로 마무리하고, 신규 `flatMap` 확장 함수는 만들지 않는다. (사용자의 "1 맞음" 응답을 이렇게 해석한 것이며, 해석임을 명시한다.) | 본문, 사용자 결정(해석) |
| FR-5 | 자체 `Result.kt`를 삭제한다. | 본문 |
| FR-6 | 자체 `Result`에 의존하는 관련 테스트를 수정한다. `Result.Success` / `Result.Failure` 생성은 `Result.success` / `Result.failure`로, `(x as Result.Failure).throwable`은 `exceptionOrNull()`로 바꾸고, `equalTo` 비교는 유지한다. mockk의 `returns`에 value class(`kotlin.Result`)를 쓸 때의 동작과 Jacoco 영향은 검증 대상으로 기록하고 확인한다. | 본문, 사용자 결정 |
| FR-7 | `core:kotlinx:coroutines`에 `suspendRunCatching`을 추가한다(`core:kotlin` 아님, 사용자 결정). `CancellationException`은 다시 던지고, 그 외 `Exception`만 `Result.failure`로 감싼다. 순수 Kotlin 모듈이므로 android `Log`를 쓰지 않는다. `suspend` 블록에서 기존 `catching`을 대체하는 지점은 이것을 사용하며, 사용 모듈에 필요한 `core:kotlinx:coroutines` 의존성(예: `core:local`)을 추가한다. `core:kotlinx:coroutines`는 Jacoco를 적용하지 않는 모듈이며 이번에도 적용하지 않고, `suspendRunCatching`에 대한 별도 테스트도 요구하지 않는다(사용자 정정). | 사용자 결정 |
| FR-8 | 전환 후 모든 모듈 커버리지가 100%로 유지되도록(NFR-2) `TagEditTask`의 SUSPEND 우회, `@ExcludeFromGeneratedTestReport`, Jacoco 안내 주석 등 우회 코드를 처리한다. 유지, 제거, 신규 우회 중 어느 방식이든 이 목표를 달성하는 수단이며 범위 안에 포함한다. | 사용자 결정 |

## 비기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| NFR-1 | 전환 후 전체 테스트가 통과한다. | 본문 |
| NFR-2 | `master`는 모든 모듈이 Jacoco 인스트럭션·브랜치·라인 커버리지 100%이며(사용자 확인), 전환 후에도 모든 모듈이 세 카운터 모두 100%를 유지해야 한다. 이는 처리 방식과 무관한 프로젝트 규칙이다. 검증 명령은 CI와 같은 `./gradlew jacocoTestReportJvm jacocoTestReportRelease`이다. | 본문, 사용자 결정 |

## 인수 조건
- [ ] Jacoco가 `0.8.16`이며 `libs.versions.toml`과 `configureJacocoToolVersion`에 반영되어 있다.
- [ ] 표준 `Result`를 쓰는 코드가 Jacoco 리포트에 올바르게 측정된다.
- [ ] 코드베이스에 자체 `Result`(`com.nlab.reminder.core.kotlin.Result`)와 `catching` 사용이 남아 있지 않다.
- [ ] `flatMap` 사용처가 없음이 확인되어 있고 신규 `flatMap` 확장은 추가되지 않았다.
- [ ] `core/kotlin`의 `Result.kt`가 삭제되어 있다.
- [ ] `core:kotlinx:coroutines`에 `suspendRunCatching`(`CancellationException` 재던짐, 그 외 `Exception`만 `Result.failure`로 래핑)이 있다.
- [ ] `suspendRunCatching`을 쓰는 모듈(`core:local` 등)이 `core:kotlinx:coroutines`에 의존한다.
- [ ] `suspend` 블록의 기존 `catching` 대체 지점이 `suspendRunCatching`을 쓴다.
- [ ] 테스트가 `Result.success` / `Result.failure` / `exceptionOrNull()`로 수정되어 있고 mockk value class `returns` 동작이 확인되어 있다.
- [ ] `./gradlew test`가 통과한다.
- [ ] `./gradlew jacocoTestReportJvm jacocoTestReportRelease` 결과 모든 모듈이 인스트럭션·브랜치·라인 100%이다.

## 영향 범위
`master`(HEAD `9a2c0323`, Kotlin 2.3.21, mockk 1.14.11, Jacoco 0.8.14) 코드 검색으로 확인한 사항이다. 빌드/테스트는 실행하지 않았다.

### 삭제 대상
- `core/kotlin/src/main/kotlin/com/nlab/reminder/core/kotlin/Result.kt`: `Result`(`Success`/`Failure`), `catching`, `isSuccess`, `isFailure`, `getOrThrow`, `getOrNull`, `getOrElse`, `map`, `mapCatching`, `flatMap`, `onFailure`, `onSuccess`를 정의한다.
- 위 확장 함수는 `flatMap`을 제외하고 `kotlin.Result` 표준 API와 이름·형태가 대응한다. 즉 대부분의 호출부는 `core.kotlin` import 제거만으로 표준 API로 이어질 수 있다. (컴파일로 확인하지 않은 추정이다.)

### 자체 `Result` / `catching` 계열을 쓰는 main 파일 (17개)
- `core/component/tag`: `TagEditStateMachine.kt`(`map`), `TagEditTask.kt`(`core.kotlin.*` 와일드카드 import, `Result.Success` / `Result.Failure` 직접 생성)
- `core/component/usermessage`: `UserMessageResultExtensions.kt` (`Result`, `getOrThrow`)
- `core/data-di`: `AppScopeDataModule.kt` (`onSuccess`, `onFailure`)
- `core/data`: `CompletedScheduleShownRepository.kt`, `ScheduleRepository.kt`, `TagRepository.kt`
- `core/data-impl`: `CompletedScheduleShownRepositoryImpl.kt`, `LocalScheduleRepository.kt`, `LocalTagRepository.kt`, `OfflineFirstLinkMetadataRepository.kt`(`map`, `onSuccess`), `RemoteFirstTimeSnapshotRepository.kt`(`getOrNull`)
- `core/local`: `DataStores.kt`, `PreferenceDataSource.kt`
- `core/network`: `LinkThumbnailDataSource.kt`, `TrustedTimeDataSource.kt`
- `feature/home`: `HomeReduce.kt` (`Flow<Result<TagEditStateTransition>>`)

### `catching` 호출 (main 9곳, test 1곳)
- `LocalScheduleRepository` 3곳(`save`, `updateAll`, `delete`), `LocalTagRepository` 3곳, `LinkThumbnailDataSource` 1곳, `TrustedTimeDataSource` 1곳, `DataStores.kt` 1곳, test `UserMessageResultExtensionsKtTest` 1곳. 이슈의 "약 16곳"과 다르다. 해당 main 호출은 모두 `suspend` 함수 안에 있다(`DataStores.kt`의 `catching { edit(transform) }` 포함, `edit`이 suspend).

### 표준 `kotlin.Result` 기존 사용
- `master`에는 `runCatching` / `kotlin.Result` 사용처가 없다. 모든 `Result` 사용이 자체 `Result`이므로 혼재 상태는 없다.

### `Result.Success` / `Result.Failure` 사용처
- 프로덕션 코드에서 `is` 분기나 `when` 패턴 매칭은 정의부(`Result.kt`) 밖에서 찾지 못했다. 생성은 `TagEditTask.kt`에 7곳이다.
- 테스트의 생성 / `equalTo` 비교: `TagEditStateMachineTest` 14곳, `TagEditTaskKtTest` 12곳, `UserMessageResultExtensionsKtTest` 6곳, `OfflineFirstLinkMetadataRepositoryTest` 3곳, `RemoteFirstTimeSnapshotRepositoryTest` 2곳, `HomeReduceKtTest` 2곳, `LocalTagRepositoryTest` 1곳. `UserMessageResultExtensionsKtTest`는 `(catching(block) as Result.Failure).throwable`로 캐스팅한다.

### 관련 테스트 (9개)
- `TagEditStateMachineTest`, `TagEditTaskKtTest`, `UserMessageResultExtensionsKtTest`, `CompletedScheduleDetailShownRepositoryImplTest`, `LocalScheduleRepositoryTest`(`getOrThrow`, `isSuccess`), `LocalTagRepositoryTest`, `OfflineFirstLinkMetadataRepositoryTest`, `RemoteFirstTimeSnapshotRepositoryTest`, `HomeReduceKtTest`

### `flatMap` 확인 결과
- `Result.flatMap`은 `Result.kt:77`에 정의되어 있지만 코드베이스 어디에서도 호출되지 않는다. 발견된 다른 `flatMap`은 테스트 헬퍼(`InternalTagGenerator.kt`, 컬렉션)와 Gradle 빌드 로직의 `Provider.flatMap`(`AndroidCompose.kt`)이다.
- 이슈가 든 `GetScheduleListResourcesFlowUseCase`(`core/component/schedulelist/.../content/`)는 `Result`를 쓰지 않고 Flow의 `flatMapLatest`만 쓴다. `RemoteFirstTimeSnapshotRepository`는 `getOrNull()`과 Flow `flatMapConcat`을, `LocalScheduleRepository`는 `catching`을 쓴다.

### `suspendRunCatching` 배치 모듈 현황 (FR-7 관련)
- `core:kotlinx:coroutines`(`core/kotlinx/coroutines`)는 master에 존재하고 `settings.gradle.kts`에 등록되어 있으며, `build.gradle.kts`에 `api(libs.kotlinx.coroutines.core)` 의존성이 있다.
- 이 모듈은 `src/test`와 `nlab.jvm.library.jacoco` 플러그인이 없는 모듈이며(테스트가 불필요한 모듈, 사용자 확인), 이번에도 테스트 소스셋과 Jacoco를 추가하지 않는다.
- 의존 관계: `core:network`와 `core:data-impl`은 이미 `core:kotlinx:coroutines`에 의존한다. `catching`을 쓰는 `core:local`은 `core:kotlin`에만 의존하므로(`core/local/build.gradle.kts`) `core:kotlinx:coroutines` 의존성 추가가 필요하다.
- `core:kotlin`은 `suspendRunCatching` 배치 대상이 아니므로 모듈 설정 변경 대상이 아니다(`Result.kt`만 삭제).

### Jacoco 관련
- `gradle/libs.versions.toml`의 `jacoco = "0.8.14"`, `build-logic/convention/src/main/kotlin/com/nlab/reminder/Jacoco.kt`의 `configureJacocoToolVersion`(`libs.findVersion("jacoco")`로 버전을 읽으므로 버전 카탈로그만 바꾸면 반영되는 구조). CI(`.github/workflows/ci.yml`)는 `jacocoTestReportJvm`, `jacocoTestReportRelease`를 실행한다.
- 자체 `Result`/Jacoco 우회와 관련된 주석·장치: `Result.kt` KDoc, `TagEditTask.kt`의 `// prevent Missing in the Jacoco report by SUSPEND FUNCTION`(미리 만든 `Result.Success`를 반환), `UserMessageResultExtensions.kt`의 Jacoco 관련 KDoc과 `@ExcludeFromGeneratedTestReport`.

## 미결 사항 (Open Questions)
없음.

참고(결정 사항 아님, FR-2의 검증 대상): Jacoco `0.8.16`이 value class 문제를 실제로 해소한다는 이슈의 주장은 이 분석에서 검증하지 않았다.

## 충돌 및 참고
- 본문 외에 댓글과 부모 이슈가 없어 충돌하는 출처는 없다.
- 본문의 "약 20 imports", "약 16 call sites"는 근사치이며 `master` 코드와 다르다(main 파일 17개, `catching` main 9곳). 영향 범위 목록은 코드 검색으로 확인한 파일 기준이다.
- 본문의 `flatMap` 사용처 목록은 현재 코드와 일치하지 않는다(`Result.flatMap` 호출처 없음). 사용자 결정에 따라 FR-4는 "사용처 없음 확인"으로 마무리한다.

---

## 변경 이력
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-04 | 최초 작성 | 본문 |
| 2026-11-04 | 기반 브랜치가 `feature/issue601`로 바뀌어 코드 기준 내용을 재검증하고 영향 범위를 갱신. 사용자 결정 반영: FR-4를 "사용처 없음 확인"으로 확정(신규 `flatMap` 없음, "1 맞음" 응답의 해석), FR-7(`suspendRunCatching`)과 FR-8(Jacoco 우회 코드 처리) 추가, FR-3/FR-6 보완, NFR-2 강화, 인수 조건과 제외 범위 갱신. | 코드 재검증, 사용자 결정 |
| 2026-11-05 | 작업 기준을 `master`(HEAD `9a2c0323`)로 되돌려 코드 기반 주장을 재검증하고 `feature/issue601` 전용 내용을 제거(main 파일 21개 -> 17개, `catching` main 10곳 -> 9곳, 이미 `kotlin.Result`를 쓰는 혼재 코드 항목 삭제, `Result.kt`에 `flatMap` 정의가 있으나 호출처 없음으로 정정, 테스트 사용처 수 갱신). NFR-2를 "`master` 기준 모든 모듈 인스트럭션·브랜치·라인 100% 유지"로 변경하고 기준선(feature/issue601 HEAD) 및 master 워크트리 재생성 대안 문구 삭제, 검증 명령 명시. FR-7과 영향 범위에 `core:kotlin`의 coroutines 의존성·테스트 소스셋·Jacoco 부재 반영, 커버리지 카운터 질문 해소, PR base `master` 명시. Open Questions를 `core:kotlin` 기존 코드 커버리지/배치 1건으로 교체. | 코드 재검증, 사용자 결정 |
| 2026-11-05 | 사용자 결정 반영: `suspendRunCatching` 배치를 `core:kotlin`에서 `core:kotlinx:coroutines`로 변경(FR-7, 범위, 영향 범위, 인수 조건 정정). 해당 모듈은 존재하고 coroutines 의존성이 있으나 `src/test`와 Jacoco가 없고 `core:local`이 의존하지 않음을 확인해 반영. 관련 Open Question 해소. | 사용자 결정, 코드 재검증 |
| 2026-11-05 | 사용자 정정 반영: `core:kotlinx:coroutines`는 Jacoco 비적용 모듈이며 `suspendRunCatching`에 테스트가 불필요하므로, 해당 모듈의 테스트 소스셋·Jacoco 신규 적용, 기존 코드 100% 확보, 새 분기 테스트 요구를 FR-7, 범위, 인수 조건, 영향 범위에서 제거. 사용 모듈 의존성 추가만 유지. | 사용자 결정 |
