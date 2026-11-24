# Plan: [Task] Update compileSdk to 37.1+ for Compose BOM 2026.11.01 upgrade (#937)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/937
- Requirements: docs/spec/issue937/requirements.md
- Created: 2026-11-24
- Last Updated: 2026-11-24

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
- AGP 9.5.1 이 37.1 을 지원하지 않을 때만 AGP / Gradle 업그레이드를 진행한다 (최소 변경 원칙). 지원한다면 업그레이드하지 않는다.
- `targetSdk` (35) 는 변경하지 않는다. 이슈가 `compileSdk` 만 언급한다.
- 별도 브랜치(`feature/issue937`)의 PR 에 BOM 업그레이드를 함께 포함한다. #934 브랜치 위에서 작업하지 않으며, #934 처리(close 등)는 PR 단계에서 다룬다.
- "release 빌드" 검증은 CI 가 수행하는 범위(`assembleDebug`, `jacocoTestReportJvm`, `jacocoTestReportRelease`)와 `assembleRelease`(minify / R8 포함)로 한다.
- Compose 1.13 에서 deprecation 외의 신규 빌드 오류가 나오면, 빌드를 막는 것만 본 이슈에서 최소 수정하고 나머지는 별도 이슈로 분리한다.

## Architecture/Design
1. **버전 카탈로그**: `gradle/libs.versions.toml` 의 `compileSdk` 를 문자열 `"37.1"` 로 바꾸고 `androidxComposeBom` 을 `"2026.11.01"` 로 바꾼다. 카탈로그 버전은 문자열이므로 `"37.1"` 은 그대로 표현 가능하다.
2. **Convention plugin 파싱**: `build-logic/convention/src/main/kotlin/com/nlab/reminder/AndroidSdk.kt` 의 `compileSdk = ....toInt()` 는 `"37.1"` 을 파싱하지 못한다. 문자열을 `.` 으로 분리해 major / minor 를 얻는 작은 파서(internal 함수, 예: `"37.1"` -> major 37, minor 1, `"37"` -> major 37, minor 없음)를 추가하고, AGP 의 minor API level 을 지원하는 `compileSdk { }` DSL 블록(`release(major) { minorApiLevel = minor }` 형태로 알려져 있으나 AGP 9.5.1 의 정확한 시그니처는 Task 1 에서 확인)으로 설정한다.
   - 파서는 순수 함수로 두어 `build-logic` 에서 단위 테스트가 가능하도록 한다. build-logic 에 테스트 소스셋 / 의존성이 현재 없으므로(`build-logic/convention/src` 에 `main` 만 존재), 테스트 추가 방식은 Task 1 의 확인 결과로 결정한다.
   - `minSdk`, `targetSdk` 는 그대로 `toInt()` 를 유지한다.
3. **AGP / Gradle**: 먼저 AGP 9.5.1 + Gradle 9.9.1 로 37.1 이 동작하는지 확인한다. 동작하지 않을 때만 업그레이드를 검토한다 (Questions 참고).
4. **SDK 플랫폼**: 로컬에 `android-37.1` 플랫폼이 없다 (`~/Library/Android/sdk/platforms` 에 android-37.0 까지). 빌드 시 자동 다운로드되는지 확인하고, 안 되면 SDK 설치가 필요하다. CI 환경의 SDK 설치 가능 여부도 확인 대상이다.

## Tasks
1. AGP 9.5.1 의 `compileSdk` minor API level DSL 을 확인한다 (정확한 시그니처, 37.1 지원 여부, 필요 시 SDK 플랫폼 android-37.1 확보 방법). 코드 변경 없이 결론만 기록한다.
   - **Verify**: 확인 결과(사용 DSL, AGP 업그레이드 필요 여부)를 작업 메모로 남기고, 업그레이드가 필요하면 구현 전에 Questions 로 반환한다.
2. `compileSdk` 문자열 파싱 로직(major / minor)을 먼저 테스트로 작성한다 (`"37"`, `"37.1"`, 잘못된 형식 `"37.x"`, `"37.1.2"` 등).
   - **Verify**: build-logic 대상 테스트 명령(예: `./gradlew -p build-logic :convention:test`)이 실패(Red)로 시작한다. 테스트 인프라 추가가 필요하면 최소한으로 구성한다.
3. 파서를 구현하고 `AndroidSdk.kt` 의 `compileSdk` 설정을 minor 지원 DSL 로 교체한다.
   - **Verify**: Task 2 의 테스트 통과, `./gradlew -p build-logic :convention:build` 성공.
4. `gradle/libs.versions.toml` 을 수정한다: `compileSdk = "37.1"`, `androidxComposeBom = "2026.11.01"`.
   - **Verify**: `./gradlew :app:checkDebugAarMetadata` 성공 (FR-6).
5. Compose 1.13 업그레이드로 빌드를 막는 신규 오류가 있으면 최소 수정한다 (deprecation warning 처리는 제외, NFR-1).
   - **Verify**: `./gradlew :app:assembleDebug` 성공. 오류가 없으면 변경 없음으로 기록.
6. 전체 unit test 와 release 빌드를 실행해 검증한다.
   - **Verify**: `./gradlew test`, `./gradlew :app:assembleRelease`, `./gradlew jacocoTestReportJvm jacocoTestReportRelease` 성공 (FR-4, FR-5).
7. 변경 범위 점검: deprecation 처리 코드가 포함되지 않았고, `targetSdk` / `minSdk` 가 변하지 않았음을 diff 로 확인한다.
   - **Verify**: `git diff --stat` 이 `libs.versions.toml`, `AndroidSdk.kt`(및 그 테스트, 필요 시 build-logic 설정)로 한정됨.

## Verification
testStrategy: unit

- 이 변경은 빌드 스크립트와 의존성 버전만 다루며 Compose / layout UI 코드를 수정하지 않으므로 Maestro UI QA 는 하지 않는다.
- 범위: build-logic 의 파서 단위 테스트, 전체 `./gradlew test` (모든 모듈이 BOM 과 compileSdk 영향을 받으므로 전체 실행), 빌드 검증.
- 수용 기준 대응:
  - `compileSdk` 정확히 37.1: `libs.versions.toml` diff 와 빌드 결과(AGP 가 android-37.1 을 사용)로 확인.
  - minor 파싱 오류 없음: Task 2 / 3 의 테스트.
  - BOM 2026.11.01: `libs.versions.toml`.
  - `:app:checkDebugAarMetadata`, Debug / Release 빌드: Task 4, 6.
  - 모든 unit test: Task 6.
  - deprecation 처리 미포함: Task 7.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-24 | Initial draft | requirements.md |
