# 요구사항 분석: [Task] Update legacy device spec in Previews to new DeviceSpec format (#930)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/930
- Parent issue: None
- Labels: Compose, AX
- Created: 2026-11-17
- Last Updated: 2026-11-25
- Analysis Baseline: 2026-11-25T03:05:47Z

## 배경과 문제
`core/androidx/compose/.../Previews.kt`의 `@Previews`는 legacy device spec 형식(`spec:shape=Normal,width=360,height=640,unit=dp,dpi=480`)을 사용한다. `android preview render`가 이 형식을 거부하므로 CLI로 preview를 렌더링할 수 없다.

## 목표
`@Previews`의 device spec을 새 DeviceSpec 형식으로 바꿔 기존 preview를 `android preview render`로 렌더링할 수 있게 한다.

## 범위
### 포함
- `@Previews`의 `device` 값을 새 형식으로 변경
- 기존 preview에 대한 `android preview render` 성공 확인

### 제외
- N/A (이슈에 명시된 제외 범위 없음)

## 기능 요구사항
| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | `@Previews`의 `device` 값을 `spec:width=360dp,height=640dp,dpi=480`로 변경한다. | Body (Needs) |
| FR-2 | 기존 preview에 대해 `android preview render`가 성공하는지 검증한다. | Body (Needs) |

## 비기능 요구사항
| ID | Requirement | Source |
|----|-------------|--------|
| - | N/A (이슈에 명시된 비기능 요구사항 없음) | - |

## 인수 조건
- [ ] `@Previews`에 선언된 `device` 값이 `spec:width=360dp,height=640dp,dpi=480`이다 (FR-1).
- [ ] 기존 preview에 대한 `android preview render` 실행이 성공한다 (FR-2). 검증 대상 범위는 미결 질문 1에서 결정한다.

## 영향 범위
코드로 확인한 범위:
- 변경 대상: `core/androidx/compose/src/main/kotlin/com/nlab/reminder/core/androidx/compose/ui/tooling/preview/Previews.kt`
  - `phone`(28행), `phone-dark`(34행) 두 `@Preview`가 같은 legacy spec을 사용한다.
  - 저장소의 Kotlin 파일에서 `spec:` 형식의 device 값은 이 두 곳뿐이다.
- `@Previews` 사용처: 6개 모듈, preview 함수 15개
  - `:core:androidx:compose`: `HeadBlurLayer.kt`(1), `IconButton.kt`(1)
  - `:core:component:schedulelist`: `ScheduleListToolbar.kt`(2)
  - `:core:component:tag`: `TagDialogButtons.kt`, `TagEditTaskSelectionDialog.kt`, `TagMergeDialog.kt`, `TagRenameDialog.kt`, `TagCard.kt`(각 1)
  - `:core:component:toolbar`: `CompleteButton.kt`, `Title.kt`(각 1)
  - `:feature:all:impl`: `AllScreen.kt`(1)
  - `:feature:home:impl`: `HomeScreen.kt`(3)
- `@Previews`를 거치지 않고 `@Preview`를 직접 쓰는 곳(`device` 지정 여부는 확인한 선언 기준으로 없음): `ColorPressButton.kt`, `core/designsystem`의 `BasePlaneatBottomSheet.kt`, `PlaneatBottomSheet.kt`, `PlaneatDialog.kt`, `PlaneatDropdownMenu.kt`
- 이 분석에서는 `android preview render`를 실행하지 않았다. 거부 현상과 변경 후 성공 여부는 확인되지 않았다. 로컬에는 `android` CLI(버전 1.1.16919734)가 있고 `preview list`, `preview render` 명령을 제공한다.

## 미결 질문
1. FR-2의 "existing previews" 범위: `@Previews`를 사용하는 15개 함수 전부인가, `@Preview`를 직접 선언한 preview까지 포함한 전체인가, 모듈별 대표 일부인가?
2. device spec과 무관한 원인(preview 함수의 접근 제한자, 모듈 설정, CLI 제약 등)으로 렌더링이 실패하는 preview가 있으면, 이 이슈에서 함께 수정하는가, 별도 이슈로 분리하는가?
3. "render 성공"의 판정 기준: 명령 성공과 PNG 생성만 확인하면 되는가, 변경 전후의 크기·밀도 등 화면 결과가 같은지도 확인해야 하는가?
4. 검증 결과를 어디에 남기는가(예: PR 설명)? 렌더링 명령의 사용법을 문서화하거나 검증을 자동화(CI 등)하는 작업은 필요한가? (이슈에는 언급이 없다.)

## 상충 사항과 참고
- 코멘트와 parent issue가 없어 상충하는 내용은 없다. 모든 요구사항의 출처는 이슈 본문이다.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-25 | Initial draft | Body |
