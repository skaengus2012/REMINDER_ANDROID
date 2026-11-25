# Plan: [Task] Update compileSdk to 37.1+ for Compose BOM 2026.11.01 upgrade (#937)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/937
- Requirements: docs/spec/issue937/requirements.md
- Created: 2026-11-24
- Last Updated: 2026-11-25

## Context/Goals
Dependabot PR #934 이 `androidx.compose:compose-bom` 을 2026.09.00 에서 2026.11.01 로 올리지만, Compose 1.13.0 라이브러리들이 API 37.1 이상으로 컴파일하기를 요구해 `:app:checkDebugAarMetadata` 가 실패한다. 현재 프로젝트는 `compileSdk = "37"` (android-37.0) 이다.
목표: `compileSdk` 를 정확히 37.1 로 올리고 Compose BOM 2026.11.01 을 적용하여, 빌드와 테스트가 통과하도록 한다.

## Requirements
requirements.md 의 FR-1 ~ FR-7, NFR-1 을 따른다.
- FR-1: `compileSdk` 는 정확히 37.1 (37.2 이상, 38 불가).
- FR-2: AGP / Gradle 호환성 검증, 필요 시 조정.
- FR-3: Compose BOM 2026.11.01.
- FR-4: Debug / Release 빌드 성공.
- FR-5: 모든 unit test 통과.
- FR-6: `:app:checkDebugAarMetadata` 성공.
- FR-7: 버전 카탈로그와 convention plugin 이 minor version(37.1)을 표현/파싱할 수 있어야 한다.
- NFR-1: Compose 1.13 deprecation warning 처리는 본 이슈 범위 밖.

### Accepted Decisions / Assumptions (Open Questions, 사용자가 승인 시점에 그대로 수용)
requirements.md 의 Open Questions 는 사용자가 승인하며 아래와 같이 수용되었다. 이 계획은 기본 가정으로 다음을 따른다.
- AGP 가 37.1 을 지원하지 않을 때만 AGP / Gradle 업그레이드를 진행한다 (최소 변경 원칙). 지원한다면 본 이슈에서는 업그레이드하지 않는다. 브랜치의 AGP 9.5.2 / Gradle 9.9.2 는 `master` 병합으로 들어온 것이다.
- `targetSdk` (35) 는 변경하지 않는다. 이슈가 `compileSdk` 만 언급한다.
- 별도 브랜치(`feature/issue937`)의 PR 에 BOM 업그레이드를 함께 포함한다. #934 브랜치 위에서 작업하지 않으며, #934 처리(close 등)는 PR 단계에서 다룬다.
- "release 빌드" 검증은 CI 가 수행하는 범위(`assembleDebug`, `jacocoTestReportJvm`, `jacocoTestReportRelease`)와 `assembleRelease`(minify / R8 포함)로 한다.
- Compose 1.13 에서 deprecation 외의 신규 빌드 오류가 나오면, 빌드를 막는 것만 본 이슈에서 최소 수정하고 나머지는 별도 이슈로 분리한다.

## Architecture/Design
1. **버전 카탈로그**: `gradle/libs.versions.toml` 의 `compileSdk` 를 문자열 `"37.1"` 로 바꾸고 `androidxComposeBom` 을 `"2026.11.01"` 로 바꾼다. 카탈로그 버전은 문자열이므로 `"37.1"` 은 그대로 표현 가능하다.
2. **Convention plugin 파싱**: `build-logic/convention/src/main/kotlin/com/nlab/reminder/AndroidSdk.kt` 의 `compileSdk = ....toInt()` 는 `"37.1"` 을 파싱하지 못한다. 별도 타입이나 파서 함수 없이 같은 파일에서 `split(".", limit = 2).map(String::toInt)` 로 major / minor 를 얻고, AGP 의 `compileSdk { }` DSL 블록(`release(major) { minorApiLevel = minor }`)으로 설정한다. minor 가 없으면 `minorApiLevel` 을 설정하지 않는다.
   - `limit = 2` 이므로 `"37.x"`, `"37.1.2"` 같은 값은 `toInt()` 에서 `NumberFormatException` 으로 실패한다.
   - 재사용되지 않는 빌드 설정 코드이므로 build-logic 단위 테스트는 두지 않는다 (PR review).
   - `minSdk`, `targetSdk` 는 그대로 `toInt()` 를 유지한다.
3. **AGP / Gradle**: 본 이슈에서는 업그레이드하지 않는다. 37.1 은 AGP 9.5.1 + Gradle 9.9.1 기준으로 적용했고, 이후 `master` 병합으로 AGP 9.5.2 + Gradle 9.9.2 가 되었다.
4. **SDK 플랫폼**: 로컬에 `android-37.1` 플랫폼이 없다 (`~/Library/Android/sdk/platforms` 에 android-37.0 까지). 빌드 시 자동 다운로드되는지 확인하고, 안 되면 SDK 설치가 필요하다. CI 환경의 SDK 설치 가능 여부도 확인 대상이다.

## Tasks
1. AGP 의 `compileSdk` minor API level DSL 을 확인한다 (정확한 시그니처, 37.1 지원 여부, 필요 시 SDK 플랫폼 android-37.1 확보 방법). 코드 변경 없이 결론만 기록한다.
   - **Verify**: 확인 결과(사용 DSL, AGP 업그레이드 필요 여부)를 작업 메모로 남기고, 업그레이드가 필요하면 구현 전에 Questions 로 반환한다.
2. (Dropped) `compileSdk` 파싱 로직의 build-logic 단위 테스트 작성. 사유: 비즈니스 로직이 아니고 파서를 별도 함수로 두지 않기로 함 (PR review).
3. `AndroidSdk.kt` 에서 major / minor 를 직접 파싱하고 `compileSdk` 설정을 minor 지원 DSL 로 교체한다.
   - **Verify**: `./gradlew -p build-logic :convention:build` 성공.
4. `gradle/libs.versions.toml` 을 수정한다: `compileSdk = "37.1"`, `androidxComposeBom = "2026.11.01"`.
   - **Verify**: `./gradlew :app:checkDebugAarMetadata` 성공 (FR-6).
5. Compose 1.13 업그레이드로 빌드를 막는 신규 오류가 있으면 최소 수정한다 (deprecation warning 처리는 제외, NFR-1).
   - **Verify**: `./gradlew :app:assembleDebug` 성공. 오류가 없으면 변경 없음으로 기록.
6. 전체 unit test 와 release 빌드를 실행해 검증한다.
   - **Verify**: `./gradlew test`, `./gradlew :app:assembleRelease`, `./gradlew jacocoTestReportJvm jacocoTestReportRelease` 성공 (FR-4, FR-5).
7. 변경 범위 점검: deprecation 처리 코드가 포함되지 않았고, `targetSdk` / `minSdk` 가 변하지 않았음을 diff 로 확인한다.
   - **Verify**: `git diff --stat` 이 `libs.versions.toml`, `AndroidSdk.kt`, `docs/spec/issue937/` 로 한정됨.

## Verification
testStrategy: unit

- 이 변경은 빌드 스크립트와 의존성 버전만 다루며 Compose / layout UI 코드를 수정하지 않으므로 Maestro UI QA 는 하지 않는다.
- 범위: 전체 `./gradlew test` (모든 모듈이 BOM 과 compileSdk 영향을 받으므로 전체 실행), 빌드 검증. build-logic 단위 테스트는 두지 않는다.
- 수용 기준 대응:
  - `compileSdk` 정확히 37.1: `libs.versions.toml` diff 와 빌드 결과(AGP 가 android-37.1 을 사용)로 확인.
  - minor 파싱 오류 없음: Task 3 의 build-logic 빌드와 Task 4 의 `:app:checkDebugAarMetadata`.
  - BOM 2026.11.01: `libs.versions.toml`.
  - `:app:checkDebugAarMetadata`, Debug / Release 빌드: Task 4, 6.
  - 모든 unit test: Task 6.
  - deprecation 처리 미포함: Task 7.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-24 | Initial draft | requirements.md |
| 2026-11-25 | 파서를 `AndroidSdk.kt` inline 파싱으로 변경, build-logic 단위 테스트 제외 (Task 2 dropped, Task 3 / 7, Verification), `master` 병합에 따른 AGP / Gradle 버전 반영 | PR #938 review |
