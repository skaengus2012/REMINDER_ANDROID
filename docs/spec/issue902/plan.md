# Plan: [Bug] Hidden items appear in the mirror view while dragging in a scrollable list (#902)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/902
- Requirements: docs/spec/issue902/requirements.md
- Created: 2026-11-04
- Last Updated: 2026-11-04

## 배경 및 목표
- 스크롤 목록에서 아이템을 드래그하면 미러 뷰에 숨겨져 있어야 할 뷰(빈 링크 썸네일 뷰, info 버튼)의 자리가 마진으로 남아 레이아웃이 벌어지고 태그 텍스트가 밀린다.
- 목표: 드래그 중 미러 뷰가 원본 아이템과 동일하게 보이도록 한다.

## 요구사항
requirements.md 의 요구사항을 그대로 따른다. 가장 보수적으로 해석하여 이슈에 명시된 것만 수정한다.

| ID | 요구사항 |
|----|----------|
| FR-1 | 드래그 중 미러 뷰는 원본 아이템과 동일하게 보여야 한다. |
| FR-2 | 숨겨진 뷰(빈 링크 썸네일 뷰, info 버튼)는 미러 뷰에서 마진이나 공간을 차지하지 않아야 한다. |
| FR-3 | 드래그 중 태그 텍스트가 밀리지 않아야 한다. |

### 가정 (사용자가 Open Questions 를 그대로 수용함)
- "숨겨진 뷰"의 범위는 이슈에 명시된 빈 링크 썸네일 뷰와 info 버튼으로 한정한다.
- 드래그 시작 시 의도된 시각 변화(`alpha = 0.8f`, `cardElevation = 8dp`)는 기존 동작을 유지하며 이번 수정 대상이 아니다.
- 대상 화면은 `ContentViewHolder` 를 사용하는 모든 스크롤 목록 화면이며, 화면별 분기는 하지 않는다.
- 태그 텍스트는 `edittextDetail` 에 표시되는 태그 문자열로 간주한다 (미러 레이아웃에 태그 전용 뷰 없음). 위치가 밀리는 현상은 `edittextTitle` 의 `layout_constraintEnd_toStartOf="@id/button_info"` 와 `edittextDetail` 이 title 의 폭을 따르는 제약 때문에 info 버튼이 보일 때 폭이 줄어드는 것이 원인이라고 추정한다. 구현 중 실제 재현으로 확인한다.
- PR base 브랜치는 부모 이슈가 없으므로 `master` 로 한다. (확정은 사용자 확인 필요, 아래 질문 참고)

## 아키텍처/설계
### 원인 분석 (코드 확인 결과)
- 대상 파일 (모두 `core/component/schedulelist`)
  - `src/main/kotlin/com/nlab/reminder/core/component/schedulelist/ui/content/ContentViewHolder.kt` 의 `DraggableViewHolderDelegate.mirrorView()`
  - `src/main/res/layout/layout_schedule_adapter_item_content_mirror.xml`
- 원본 아이템은 `bindCard`/`bind` 에서 `cardLink`, `textviewTitleLink`, `imageviewBgLinkThumbnail` 의 가시성을 데이터에 맞춰 바꾸고, `buttonInfo` 는 입력 포커스 여부에 따라 `isVisible` 을 바꾼다.
- 그러나 `mirrorView()` 는 `textviewTitleLink` 와 `imageviewBgLinkThumbnail` 의 가시성만 복사하고, `cardLink` 와 `buttonInfo` 의 가시성은 복사하지 않는다.
- 또한 `applyStateToMirror()` 는 미러 title 의 `goneEndMargin` 을 선택 모드가 아닐 때 0 으로 강제했다. 원본은 선택 전환이 일어나기 전까지 레이아웃 기본값(`horizontal_padding_medium`)을 유지하므로 미러의 텍스트 폭이 달라진다.
- 미러 뷰는 `DraggingMirrorViewPool` 로 재사용되므로, 이전 드래그의 상태가 남을 수 있다.
- 미러 레이아웃에서 `card_link`(`layout_marginTop=10dp`), `view_line`(`layout_marginTop=15dp`), `edittext_title`(`layout_goneMarginEnd`) 의 마진/제약은 대상 뷰가 `GONE` 이어야 의도대로 동작한다.

### 수정 방향
1. `ContentSelectionAnimDelegate.applyStateToMirror()` 에서 원본 `layoutContent` 를 `ConstraintSet.clone()` 한 뒤 미러 `layoutContent` 에 `applyTo()` 한다. 원본의 제약, margin/`goneMargin`, guideline 위치, visibility, alpha, translation 이 드래그 시작 시점 기준으로 한 번에 미러에 반영된다. (풀링된 뷰의 잔여 상태 방지)
   - 이전에는 `selectable` 플래그로 `goneEndMargin`/`guideEnd` 를 계산하고 버튼 alpha/translationX 를 개별 복사했다. 특히 미러의 `goneEndMargin` 을 0 으로 강제해 원본(레이아웃 기본값 유지)보다 텍스트 폭이 넓어지는 것이 태그 텍스트가 다르게 줄바꿈되는 직접 원인이었다.
2. `layoutContent` 의 직계 자식이 아닌 뷰(`cardLink` 내부의 `textviewTitleLink`, `imageviewBgLinkThumbnail`)는 `ConstraintSet` 범위 밖이므로 `mirrorView()` 에서 기존처럼 개별 복사한다.
3. 두 XML 을 비교한 결과 `layoutContent` 내부의 차이는 상세 입력창 타입(`ScheduleDetailsEditText` vs `EditText`)과 `view_line`(background, `verticalBias`)뿐이라 XML 은 변경하지 않는다. 미러 XML 직계 자식의 제약은 런타임에 원본 값으로 덮어써진다.
4. View 바인딩 자체는 모듈에 Robolectric 이 없어 JVM 단위 테스트가 불가능하다. 동기화 규칙을 인위적으로 분리하지 않고 실기기에서 확인한다.

### 비범위
- 미러 뷰 alpha/elevation 등 의도된 시각 효과 변경
- 이슈에 명시되지 않은 다른 숨김 뷰(메모, 상세 텍스트 등)를 의도해서 추가하는 동기화. 다만 `ConstraintSet` 적용으로 직계 자식인 `edittext_note`/`edittext_detail` 의 visibility 도 함께 원본과 같아진다.
- Compose 코드 변경

## 작업 목록
1. 재현 및 원인 확정: 원본/미러 XML 을 비교하고, 링크가 없는 아이템 및 info 버튼 숨김 상태에서 마진 차이가 발생하는 지점을 정리한다.
   - **Verify**: 원인이 미러의 `goneEndMargin` 0 강제와 `buttonInfo`/`cardLink` visibility 미동기화임을 확인한다.
2. `applyStateToMirror()` 를 `ConstraintSet` clone/apply 방식으로 교체하고, `mirrorView()` 의 `buttonInfo`/`cardLink` 개별 복사는 제거한다. (FR-1, FR-2, FR-3)
   - **Verify**: `./gradlew :core:component:schedulelist:compileDebugKotlin`
3. 변경된 모듈의 기존 단위 테스트 전체 실행.
   - **Verify**: `./gradlew :core:component:schedulelist:test`
4. 실기기 확인: 링크 없는 아이템, 링크와 썸네일이 있는 아이템, 링크는 있으나 썸네일이 없는 아이템, info 버튼이 숨겨진 아이템, 노트/일정이 없는 아이템, 선택 모드를 켰다 끈 뒤의 아이템을 각각 드래그하여 미러 뷰 외형과 태그 텍스트 위치를 원본과 비교한다.
   - **Verify**: 드래그 전후 태그 텍스트 위치 동일, 미러 뷰에 불필요한 여백 없음

## 검증
testStrategy: unit+ui

- 선택 사유: 변경이 레이아웃 XML 과 뷰 바인딩(UI)에 해당하여 단위 테스트만으로는 FR-1~FR-3 을 증명할 수 없다. 모듈에 Robolectric 이 없어 View 동작은 JVM 에서 검증되지 않는다.
- 단위 테스트: `./gradlew :core:component:schedulelist:test` (변경 모듈 한정)
- 빌드: `./gradlew :app:assembleDebug`
- UI QA (실기기, 수동): 인수 조건 4개를 시나리오로 확인한다.
  - 링크 썸네일이 없는 아이템 드래그 시 미러 뷰에 여백이 없다. (FR-2)
  - info 버튼이 숨겨진 아이템 드래그 시 미러 뷰에 여백이 없다. (FR-2)
  - 드래그 시작 전후 태그 텍스트 위치가 같다. (FR-3)
  - 미러 뷰와 원본의 외형이 같다 (의도된 alpha/elevation 제외). (FR-1)

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-04 | 초안 작성 | requirements.md |
| 2026-11-04 | 수정 방향을 개별 복사에서 `ConstraintSet` clone/apply 로 변경하고, XML 비교 결과·작업 목록·검증(수동 확인)을 현재 구현에 맞게 갱신 | 구현 중 검토 |
