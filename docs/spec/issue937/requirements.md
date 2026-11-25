# Requirements Analysis: [Task] Update compileSdk to 37.1+ for Compose BOM 2026.11.01 upgrade (#937)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/937
- Parent issue: None
- Labels: buildScript, dependencies
- Created: 2026-11-24
- Last Updated: 2026-11-24
- Analysis Baseline: 2026-11-24T00:27:03Z

## Background and Problem
Dependabot PR #934 bumps `androidx.compose:compose-bom` from 2026.09.00 to 2026.11.01, but its CI fails. The Compose 1.13.0 libraries pulled in by this BOM (e.g. `androidx.compose.animation:animation-android`, `androidx.compose.foundation:foundation-layout-android`; 6 issues in total) require compiling against Android API 37.1 or later. The project is currently compiled against android-37.0, so `:app:checkDebugAarMetadata` fails. Dependabot cannot resolve this on its own, so a manual adjustment is required.

## Goals
Raise the compile SDK so that the Compose BOM 2026.11.01 upgrade (#934) can be applied and the project builds and passes its tests.

## Scope
### In Scope
- Raise `compileSdk` to exactly 37.1.
- Make the project's `compileSdk` representation/parsing support a minor version (37.1).
- Verify AGP / Gradle compatibility, and adjust if required.
- Apply the Compose BOM 2026.11.01 upgrade.
- Confirm debug/release builds and all unit tests pass.

### Out of Scope
- Compose 1.13 deprecation warnings (e.g. BottomSheet state APIs, `BasicTextField(value, ...)`); to be handled in a separate issue.

## Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | `compileSdk` is raised to exactly 37.1 (not 37.2 or higher, not 38). | Body (Needs); User decision (2026-11-24) |
| FR-2 | AGP / Gradle compatibility with the raised `compileSdk` is verified, and adjusted if required. | Body (Needs) |
| FR-3 | The Compose BOM is upgraded to 2026.11.01. | Body (Needs) |
| FR-4 | Debug and release builds succeed after the change. | Body (Needs) |
| FR-5 | All unit tests pass after the change. | Body (Needs) |
| FR-6 | `:app:checkDebugAarMetadata` no longer fails. | Body (Problem) |
| FR-7 | The way `compileSdk` is represented and parsed in the build configuration (version catalog and convention plugin) supports a minor version, so that 37.1 can be set. The concrete mechanism is a plan-level detail. | User decision (2026-11-24) |

## Non-Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| NFR-1 | Compose 1.13 deprecation warnings are not handled in this issue (separate issue). | Body (Needs) |

## Acceptance Criteria
- [ ] `compileSdk` is exactly 37.1.
- [ ] The build configuration can represent and parse the minor version (37.1) without error.
- [ ] `androidxComposeBom` is 2026.11.01.
- [ ] `:app:checkDebugAarMetadata` succeeds.
- [ ] Debug and release builds succeed.
- [ ] All unit tests pass.
- [ ] No Compose 1.13 deprecation warning handling is included in the change.

## Impact
Confirmed from the code:
- `gradle/libs.versions.toml`: `compileSdk = "37"`, `targetSdk = "35"`, `androidGradlePlugin = "9.5.1"`, `androidxComposeBom = "2026.09.00"`.
- `build-logic/convention/src/main/kotlin/com/nlab/reminder/AndroidSdk.kt` reads `compileSdk` with `.toString().toInt()` (line 27), so a value such as `"37.1"` cannot be parsed as-is (see FR-7).
- Gradle wrapper: 9.9.1.
- Related PR: #934 (Dependabot BOM bump).

## Open Questions
- Is an AGP / Gradle upgrade acceptable if the current AGP 9.5.1 does not support compileSdk 37.1? If so, to which version?
- Should `targetSdk` (currently 35) stay unchanged? The issue mentions only `compileSdk`.
- Should the change be applied on top of #934's branch (Dependabot), or in a separate PR that includes the BOM bump (with #934 closed)?
- Does "release builds" verification include `assembleRelease` / lint / minify (R8) tasks, or only compilation? Which release-related tasks must pass?
- If new build errors or warnings other than deprecations appear with Compose 1.13 (e.g. behavior changes, API removals), should they be fixed in this issue or split out?

## Conflicts and Notes
- The issue has no comments and no parent issue, so there are no conflicts between sources.
- Decided (User decision, 2026-11-24): the `compileSdk` target is exactly 37.1. The issue's wording "37.1 or later" is narrowed to this value. How 37.1 is expressed (replacing `toInt()` in `AndroidSdk.kt`) is left to the plan; the requirement is only that minor versions be supported (FR-7).
- The issue states that 6 Compose 1.13.0 libraries are affected; only two are named as examples.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-24 | Initial draft | Body |
| 2026-11-24 | Fixed `compileSdk` target to exactly 37.1 (FR-1, Acceptance Criteria, Scope); added FR-7 (minor version representation/parsing); resolved Open Questions on target value and on expression mechanism (mechanism deferred to plan) | User decision |
