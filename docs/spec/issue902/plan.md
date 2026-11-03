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
- 미러 뷰는 `DraggingMirrorViewPool` 로 재사용되므로, 이전 드래그의 상태가 남을 수 있다.
- 미러 레이아웃에서 `card_link`(`layout_marginTop=10dp`), `view_line`(`layout_marginTop=15dp`), `edittext_title`(`layout_goneMarginEnd`) 의 마진/제약은 대상 뷰가 `GONE` 이어야 의도대로 동작한다.

### 수정 방향
1. `mirrorView()` 에서 원본의 `cardLink.visibility` 와 `buttonInfo.visibility` 를 미러 바인딩으로 매번 복사한다. (풀링된 뷰의 잔여 상태 방지)
2. `imageviewBgLinkThumbnail` 가 원본에서 `GONE` 일 때 미러에서도 `GONE` 이 되도록 복사를 유지하고, 마진/제약(`layout_goneMargin*`)이 원본 레이아웃과 일치하는지 두 XML 을 비교해 차이를 맞춘다.
3. 미러 XML 이 원본 XML 과 다른 부분은 의도된 차이(EditText 사용, 드래그 핸들 등)만 남기고, 가시성/마진 관련 차이는 제거한다.
4. 동기화 규칙은 단위 테스트가 가능하도록 순수 함수/값(예: 가시성 매핑)로 분리할 수 있는 경우에만 분리한다. 모듈에 Robolectric 이 없으므로 View 바인딩 자체는 JVM 단위 테스트가 불가능하다.

### 비범위
- 미러 뷰 alpha/elevation 등 의도된 시각 효과 변경
- 이슈에 명시되지 않은 다른 숨김 뷰(메모, 상세 텍스트 등) 동기화 추가
- Compose 코드 변경

## 작업 목록
1. 재현 및 원인 확정: 원본/미러 XML 을 비교하고, 링크가 없는 아이템 및 info 버튼 숨김 상태에서 마진 차이가 발생하는 지점을 정리한다.
   - **Verify**: 두 레이아웃의 차이 목록이 PR 설명에 근거로 남고, 원인이 `mirrorView()` 의 가시성 미동기화임을 확인한다.
2. `mirrorView()` 에서 `cardLink` 와 `buttonInfo` 의 가시성을 원본에서 미러로 매번 복사하도록 수정한다. (FR-2)
   - **Verify**: `./gradlew :core:component:schedulelist:compileDebugKotlin`
3. 미러 XML 의 마진/`layout_goneMargin*` 제약이 원본 XML 과 일치하도록 보정한다. (FR-1, FR-3)
   - **Verify**: `./gradlew :core:component:schedulelist:assembleDebug`
4. 가시성 동기화 규칙을 JVM 테스트 가능한 형태로 분리할 수 있는지 판단하고, 가능한 경우 분리한 로직에 단위 테스트를 추가한다. 분리가 인위적이면 분리하지 않고 사유를 PR 설명에 적는다.
   - **Verify**: `./gradlew :core:component:schedulelist:test`
5. 변경된 모듈의 기존 단위 테스트 전체 실행.
   - **Verify**: `./gradlew :core:component:schedulelist:test`
6. 에뮬레이터에서 UI 확인 (Maestro): 링크 없는 아이템, 링크가 있으나 썸네일이 없는 아이템, info 버튼이 숨겨진 아이템을 각각 드래그하여 미러 뷰 외형과 태그 텍스트 위치를 원본과 비교한다.
   - **Verify**: Maestro 플로우 실행 결과 및 스크린샷 비교 (드래그 전후 태그 텍스트 위치 동일, 미러 뷰에 불필요한 여백 없음)

## 검증
testStrategy: unit+ui

- 선택 사유: 변경이 레이아웃 XML 과 뷰 바인딩(UI)에 해당하여 단위 테스트만으로는 FR-1~FR-3 을 증명할 수 없다. 모듈에 Robolectric 이 없어 View 동작은 JVM 에서 검증되지 않는다.
- 단위 테스트: `./gradlew :core:component:schedulelist:test` (변경 모듈 한정)
- 빌드: `./gradlew :app:assembleDebug`
- UI QA (Maestro): 인수 조건 4개를 시나리오로 확인한다.
  - 링크 썸네일이 없는 아이템 드래그 시 미러 뷰에 여백이 없다. (FR-2)
  - info 버튼이 숨겨진 아이템 드래그 시 미러 뷰에 여백이 없다. (FR-2)
  - 드래그 시작 전후 태그 텍스트 위치가 같다. (FR-3)
  - 미러 뷰와 원본의 외형이 같다 (의도된 alpha/elevation 제외). (FR-1)
- 저장소에 `.maestro` 디렉터리가 없으므로 Maestro 플로우는 새로 작성해야 하며, 에뮬레이터 준비 여부는 실행 시점에 확인한다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-04 | 초안 작성 | requirements.md |
