# 계획: [Task] Migrate to Flocon 2.0.0 (recover closed dependabot PR #922) (#924)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/924
- 요구사항: docs/spec/issue924/requirements.md
- 생성일: 2026-11-17
- 최종 수정일: 2026-11-17

## Context/Goals
- Dependabot PR #922(Flocon 1.10.0 -> 2.0.0)가 `Flocon.initialize(context)` 제거로 컴파일에 실패해 닫혔다.
- Flocon을 2.0.0으로 올리고 `FloconInitializer`를 새 DSL로 마이그레이션해 `Build check`(debug, release)가 통과하게 한다.

## Requirements
- FR-1: `libs.versions.toml`의 `flocon`을 2.0.0으로 올린다.
- FR-2: 디버그 빌드에 `flocon`, `flocon-okhttp-interceptor`와 함께 `flocon-network-core`를 추가한다.
- FR-3: `FloconInitializer`를 `startFlocon(context) { install(FloconNetwork) }`로 교체한다.
- NFR-1: `:app:assembleDebug` 성공. NFR-2: CI 릴리스 체크 통과.

## Architecture/Design
Open Questions를 Flocon 2.0.0 소스(GitHub 태그 `2.0.0`), Maven Central, 공식 마이그레이션 가이드로 확인한 결과다.

| Open Question | 확인 결과 |
|---|---|
| `startFlocon`, `FloconNetwork` 패키지 | `startFlocon(context: Context, block: FloconConfiguration.() -> Unit)`는 `io.github.openflocon.flocon`(`StartFlocon.android.kt`). `FloconNetwork`는 `object FloconNetwork : FloconPluginFactory<...>`이며 `io.github.openflocon.flocon.network.core`. |
| okhttp interceptor 유지 여부 | 유지된다. `io.github.openflocon.flocon.okhttp.FloconOkhttpInterceptor`(생성자 기본 인자로 `FloconOkhttpInterceptor()` 호출 가능). `DebugAppsModule.kt`의 import는 변경 불필요. |
| `flocon-network-core` 좌표 | `io.github.openflocon:flocon-network-core`(Maven Central에서 확인). interceptor는 이 모듈을 `implementation`으로만 의존하므로 앱이 직접 추가해야 `FloconNetwork`를 쓸 수 있다. |
| 버전 공유 | 2.0.0은 `flocon-bom`으로 정렬되는 모듈 구조이며 모든 모듈이 같은 버전이다. 기존 `flocon` version.ref를 공유한다. alias는 `flocon-network-core`(기존 `flocon`, `flocon-okhttp` 명명 관례). |
| 릴리스 빌드 체크 의미 | CI `Build check`의 `release` 매트릭스(`jacocoTestReportJvm`, `jacocoTestReportRelease`)로 본다. Flocon은 `debugImplementation`이라 release 변형 코드와 무관하다. 확인은 Verification의 `assembleRelease` 선택 확인으로 한다. #875의 "릴리스에 Flocon 미포함" 조건은 `debugImplementation` 유지로 충족되므로 no-op 아티팩트는 추가하지 않는다. |
| 추가 install 대상 | `FloconNetwork`만 설치한다(이슈 범위, 요구사항 제외 없음). database, sharedprefs 등 신규 모듈은 범위 밖이며 필요하면 후속 이슈로 한다. |
| 데스크톱 앱 호환성 | 공식 가이드에 따르면 SDK와 데스크톱 앱 버전을 함께 올려야 한다(구버전 SDK와 신버전 데스크톱은 서로 연결 불가). 코드 변경은 없고 PR 설명에 "Flocon Desktop 2.0 필요"를 기재한다. |
| 테스트 요구사항 | 디버그 전용 초기화/DI 코드라 단위 테스트 대상이 아니다. 빌드와 기존 단위 테스트로 검증한다. |

변경 내용:
- `gradle/libs.versions.toml`: `flocon = "2.0.0"`, `flocon-network-core = { module = "io.github.openflocon:flocon-network-core", version.ref = "flocon" }` 추가.
- `app/build.gradle.kts`: `debugImplementation(libs.flocon.network.core)` 추가.
- `FloconInitializer.kt`: `Flocon.initialize(context)` 대신 `startFlocon(context) { install(FloconNetwork) }`, import를 `io.github.openflocon.flocon.startFlocon`, `io.github.openflocon.flocon.network.core.FloconNetwork`로 교체.

주의: `install` 확장/멤버의 정확한 선언은 `FloconConfiguration`에서 확인하지 못했고(공식 가이드는 `install(FloconNetwork)` 사용을 명시), Task 3의 컴파일로 확정한다. 필요하면 import를 조정한다.

## Tasks
1. `libs.versions.toml`의 `flocon`을 2.0.0으로 올리고 `flocon-network-core` 라이브러리 alias를 추가한다.
   - **Verify**: `grep -n flocon gradle/libs.versions.toml`에서 2.0.0과 신규 alias 확인
2. `app/build.gradle.kts`에 `debugImplementation(libs.flocon.network.core)`를 추가한다.
   - **Verify**: `./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep flocon`에서 `flocon-network-core:2.0.0` 확인
3. `FloconInitializer`를 `startFlocon(context) { install(FloconNetwork) }`로 마이그레이션한다.
   - **Verify**: `./gradlew :app:compileDebugKotlin` 성공
4. `DebugAppsModule`이 2.0.0의 `FloconOkhttpInterceptor`로 컴파일되는지 확인하고, 전체 디버그 빌드와 릴리스 변형을 확인한다.
   - **Verify**: `./gradlew :app:assembleDebug` 성공, `./gradlew :app:assembleRelease` 또는 `./gradlew jacocoTestReportRelease` 성공

## Verification
testStrategy: unit

- Compose/레이아웃 UI 변경이 없어 `unit+ui`(Maestro)는 불필요하다.
- 변경 모듈(`:app`)의 `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest` 통과.
- 릴리스 변형은 CI `release` 매트릭스와 동일하게 `./gradlew jacocoTestReportRelease`(또는 `:app:assembleRelease`)로 확인하고, 릴리스 classpath에 flocon이 없음을 `./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep -i flocon`(결과 없음)으로 확인한다.
- 실제 Flocon 데스크톱 2.0 연결 동작은 수동 확인 대상이며 자동 검증하지 않는다.

---

## Change History
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-17 | 최초 작성 (Open Questions를 Flocon 2.0.0 소스/가이드로 확인) | requirements.md |
