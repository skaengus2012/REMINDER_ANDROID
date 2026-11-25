# Plan: [Task] Update legacy device spec in Previews to new DeviceSpec format (#930)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/930
- Requirements: docs/spec/issue930/requirements.md
- Created: 2026-11-25
- Last Updated: 2026-11-25

## Context/Goals
`@Previews`의 legacy device spec(`spec:shape=Normal,width=360,height=640,unit=dp,dpi=480`)은 이슈에 따르면 `android preview render`에서 거부된다. 새 DeviceSpec 형식으로 바꿔 기존 preview를 CLI로 렌더링할 수 있게 한다. 거부 현상은 아직 재현하지 않았다.

## Requirements
- FR-1: `@Previews`의 `device` 값을 `spec:width=360dp,height=640dp,dpi=480`로 변경한다.
- FR-2: 기존 preview에 대해 `android preview render`가 성공하는지 검증한다.

- FR-2 검증 대상: `@Previews`를 사용하는 preview 함수 15개 전부. `@Preview`를 직접 선언한 preview는 대상이 아니다.
- device spec과 무관한 원인으로 렌더링이 실패하면 이 이슈에서 함께 수정한다.
- 성공 판정: `android preview render` 명령이 성공하고, 15개 함수 각각에서 `phone`과 `phone-dark` 두 variant의 PNG가 모두 생성된다. 변경 전후의 화면 결과 비교는 판정에 포함하지 않는다.
- 저장소 변경으로 해결할 수 없는 원인(예: CLI 자체의 제약)으로 렌더링이 실패하면 완료로 판정하지 않고 보고한다.
- 검증 결과는 PR 설명에만 기록한다. 문서화와 CI 작업은 하지 않는다.

## Architecture/Design
- 변경 파일: `core/androidx/compose/src/main/kotlin/com/nlab/reminder/core/androidx/compose/ui/tooling/preview/Previews.kt`
- `phone`, `phone-dark` 두 `@Preview`의 `device` 문자열만 바꾼다. `name`, `uiMode`, `showBackground`는 유지한다.
- 새 값에는 `shape=Normal`, `unit=dp`가 없고 단위를 값에 붙인다. 크기(360 x 640 dp)와 `dpi=480`은 같다.
- `git grep` 기준으로 Kotlin 파일의 `spec:` device 값은 위 두 곳뿐이다.

### 검증 대상 현황
- `@Previews` 사용 함수: 15개, 6개 모듈 (확인한 선언은 `private`)
  - `:core:androidx:compose`: `HeadBlurLayer.kt`(1), `IconButton.kt`(1)
  - `:core:component:schedulelist`: `ScheduleListToolbar.kt`(2)
  - `:core:component:tag`: `TagDialogButtons.kt`, `TagEditTaskSelectionDialog.kt`, `TagMergeDialog.kt`, `TagRenameDialog.kt`, `TagCard.kt`(각 1)
  - `:core:component:toolbar`: `CompleteButton.kt`, `Title.kt`(각 1)
  - `:feature:all:impl`: `AllScreen.kt`(1)
  - `:feature:home:impl`: `HomeScreen.kt`(3)
- 검증 대상 제외, `@Preview`를 직접 선언한 파일 (`device` 지정 없음): `:core:androidx:compose`의 `ColorPressButton.kt`, `:core:designsystem`의 `BasePlaneatBottomSheet.kt`, `PlaneatBottomSheet.kt`, `PlaneatDialog.kt`, `PlaneatDropdownMenu.kt`

### 렌더링 도구
- `/Users/doohyunnam/android-cli/android` (버전 1.1.16919734). 설치 작업은 필요 없다.
- `android preview list [--all] [--format=text|json] [<reference>] [-- <build_args>]`
- `android preview render [--fqn=<FQN>]... [--output=<dir|file>] [--component-tree] [--no-image] [--daemon] [--format=text|json] [<reference>] [-- <build_args>]`
  - 대상을 빌드한 뒤 PNG를 만든다. 기기나 IDE는 필요 없다. `--output`을 생략하면 임시 디렉터리에 쓴다.
  - `<reference>` 형식은 도움말상 `[system://][path]?[target]`이다. 이 저장소에 맞는 실제 값은 확인하지 않았다 (Task 1).
- 확인한 것은 도움말뿐이며 `list`, `render`는 실행하지 않았다. `private` preview 지원 여부도 확인되지 않았다.
- worktree에는 `local.properties`가 없다. `ANDROID_HOME`은 설정되어 있으며, 필요하면 `--sdk` 옵션을 쓸 수 있다.

## Tasks
1. 렌더링 명령을 확정하고 변경 전 상태를 재현한다. `android preview list`로 `<reference>` 형식과 대상 15개 함수의 FQN을 확인하고, 그중 하나를 `render`한다. 출력은 저장소 밖 디렉터리에 둔다.
   - **Verify**: 15개 FQN 목록, 사용한 명령, 결과(legacy spec 거부 여부와 오류 메시지)를 확보한다. 거부가 재현되지 않거나 다른 원인으로 먼저 실패하면 그대로 보고한다.
2. `Previews.kt`의 `device` 값 두 곳을 `spec:width=360dp,height=640dp,dpi=480`로 바꾼다 (FR-1).
   - **Verify**: `git grep -n "spec:" -- '*.kt'` 결과가 새 값 두 줄뿐이고, `./gradlew :core:androidx:compose:compileDebugKotlin`이 성공한다.
3. 대상 15개 함수를 모두 `android preview render`로 렌더링한다 (FR-2). 실패한 대상은 원인을 기록하고 Task 5로 넘긴다.
   - **Verify**: 15개 대상 각각의 FQN, 명령 성공/실패, `phone`과 `phone-dark`의 PNG 경로, 실패 원인이 빠짐없이 정리되어 있다.
5. Task 3의 실패를 수정하고 다시 렌더링한다. device spec과 무관한 원인도 이 이슈에서 수정한다. 수정 범위는 렌더링 성공에 필요한 변경으로 한정한다. 실패가 없으면 변경 없이 완료한다. 저장소 변경으로 해결할 수 없는 원인(예: CLI 자체의 제약)이면 완료로 판정하지 않고 보고한다.
   - **Verify**: 15개 대상 모두 `android preview render`가 성공하고 `phone`과 `phone-dark`의 PNG가 모두 생성된다. 코드나 설정을 수정한 모듈은 `testDebugUnitTest`가 성공한다.
4. 검증 결과를 PR 설명에만 기록한다 (Task 5 이후에 수행). 대상별 결과, 사용한 명령, Task 5의 수정 내용을 포함한다. 문서 파일과 CI 설정은 추가하거나 바꾸지 않는다.
   - **Verify**: PR 설명에 15개 대상의 최종 결과가 있다.

## Verification
testStrategy: unit

- 선택 이유: 계획된 변경은 preview 전용 annotation의 문자열이며 앱의 Compose/레이아웃 UI 동작을 바꾸지 않는다. Maestro는 기기에서 실행 중인 앱 화면을 확인하므로 이 변경을 검증할 수 없다.
- Unit test 범위: 변경 모듈 `:core:androidx:compose`와 Task 5에서 수정한 모듈
  - `./gradlew :core:androidx:compose:testDebugUnitTest`
  - annotation 문자열을 위한 새 unit test는 추가하지 않는다.
- FR-1: Task 2의 `git grep`과 compile 결과
- FR-2: Task 3, 5의 `android preview render` 결과 (unit test와 별도). 15개 대상 모두 명령 성공과 `phone`, `phone-dark` 두 variant의 PNG 생성이 기준이다.
- Task 5의 수정이 앱의 UI 동작에 영향을 주게 되면 `testStrategy`를 다시 검토한다.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-25 | Initial draft | requirements.md |
| 2026-11-25 | 미결 항목 확정: 15개 함수 전부 검증, 무관한 원인의 실패도 이 이슈에서 수정(Task 5 추가), 성공 기준은 render 성공과 PNG 생성, 결과는 PR 설명에만 기록. Task 1, 3, 4와 Verification 갱신 | user request |
| 2026-11-25 | 성공 기준을 함수별 `phone`, `phone-dark` PNG 모두 생성으로 확정(Task 3, 5, Verification 갱신). 저장소 변경으로 해결할 수 없는 렌더링 실패는 완료로 판정하지 않고 보고하는 것을 확정 | user request |
