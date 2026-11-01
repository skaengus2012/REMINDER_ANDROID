# 계획: New button icon RTL compatible (#382)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/382
- 요구사항: docs/spec/issue382/requirements.md
- 생성일: 2026-10-31
- 최종 수정일: 2026-11-02

## 맥락 및 목표
- RTL 레이아웃에서 "새로운 플랜" 버튼 아이콘(`ic_new_plan`)이 반전되어 로고와 상반되지 않도록, 아이콘 모양은 LTR과 동일한 방향으로 유지한다. (디자인 피드백, 2026-11-01 댓글)
- 디자이너가 퇴사했으므로 에셋 수정도 개발자가 직접 한다.
- 아이콘은 팩맨 입이 오른쪽의 `+`를 향하는 구조다. RTL에서는 버튼 내 위치만 텍스트 오른쪽으로 이동하고, 아이콘 모양은 반전하지 않는다. (이전 계획의 "전체 좌우 반전"은 디자인 피드백으로 폐기)

## 요구사항
- FR-1: RTL 환경에서 "새로운 플랜" 버튼 아이콘이 호환되어야 한다.
- FR-3: RTL에서 아이콘이 반전되어 로고와 상반되는 현상이 없어야 한다.
- FR-2: 이미지 교체가 필요하면 개발자가 직접 에셋을 제작한다.
- LTR 표시는 변경되지 않아야 한다.

## 아키텍처/설계
- `ic_new_plan`은 벡터 드로어블(`core/designsystem/src/main/res/drawable/ic_new_plan.xml`)이므로 래스터 에셋 교체는 필요 없다.
- 결정(2026-11-02, 사용자): 전체 비반전. 벡터의 `android:autoMirrored="true"`를 제거해 RTL에서도 LTR과 같은 방향(입이 오른쪽, `+` 오른쪽)으로 유지한다. 별도의 RTL용 리소스(`drawable-ldrtl`)는 만들지 않는다.
- 에셋의 오른쪽 투명 여백(뷰포트 37 중 그림은 약 27까지)이 RTL에서 텍스트와의 간격을 왜곡하므로, 뷰포트를 그림에 맞게 잘라(`viewportWidth=27`) 여백을 제거하고 `NewPlanButton`에서 `Spacer(10.dp)`로 아이콘과 글자 사이 간격을 준다. `Row`가 레이아웃 방향에 따라 자동으로 뒤집히므로 LTR/RTL 모두 글자 쪽에 간격이 생긴다.
- 사용처는 `HomeScreen.kt`의 `NewPlanButton`과 `IconButton.kt`이며, 둘 다 같은 드로어블을 쓰므로 에셋 한 곳의 수정으로 모두 적용된다.
- RTL 스크린샷(본문)은 반전된 아이콘이 로고와 상반되는 문제 상태로 본다.

## 작업
~~1.~~ (Dropped: 2026-11-01 디자인 피드백으로 반전 방식 폐기. 작업 7로 대체)
2. `NewPlanButton`의 Compose Preview에 LTR/RTL 변형(`LocalLayoutDirection = Rtl`)을 추가해 반전 결과를 시각적으로 확인할 수 있게 한다. (완료: `NewPlanButtonPreview`, `NewPlanButtonRtlPreview`)
   - **검증**: Android Studio Preview에서 LTR은 기존과 같고 RTL은 아이콘이 텍스트 오른쪽에 놓이되 반전되지 않고 LTR과 같은 방향인지 확인. (작업 7 이후 재확인)
3. JDK 21 환경에서 빌드한다. (작업 7 이후 재실행 필요)
   - **검증**: `./gradlew :core:designsystem:assembleDebug :feature:home:assembleDebug`가 성공. (완료: `:app:assembleDebug` 성공)
4. 기존 테스트가 깨지지 않았는지 확인한다. (작업 7 이후 재실행 필요)
   - **검증**: `./gradlew :feature:home:test :core:androidx:compose:test`가 성공. (완료: `testDebugUnitTest` 성공)
5. 에뮬레이터/기기에서 RTL을 강제로 켜 실제 화면을 확인한다. (개발자 옵션의 "RTL 레이아웃 방향 강제 적용") (작업 7 이후 재확인 필요)
   - **검증**: 홈 화면 "새로운 플랜" 버튼이 RTL에서 텍스트 오른쪽에 아이콘이 있고 아이콘은 반전되지 않았다. LTR 복귀 시 원래 모습이다.
~~6.~~ (Dropped: `ic_new_plan`은 `NewPlanButton`에서만 사용되며, `IconButton.kt`는 Preview에서만 참조한다.)
7. `ic_new_plan.xml`에서 `android:autoMirrored="true"`를 제거한다.
   - **검증**: `git diff core/designsystem/src/main/res/drawable/ic_new_plan.xml`에서 해당 속성 한 줄만 제거되었는지 확인.

## 검증
- 코드: 작업 3, 4의 Gradle 명령이 성공한다.
- 화면: 작업 2, 5에서 LTR 불변, RTL에서 아이콘 비반전(로고와 같은 방향)을 확인한다.
- 요구사항 문서의 인수 조건을 모두 체크한다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-10-31 | 최초 작성 | requirements.md, 사용자 요청 |
| 2026-10-31 | 작업 2 완료 표기 (HomeScreen.kt에 LTR/RTL Preview 추가) | 사용자 요청 |
| 2026-11-01 | 실기기 확인 결과 반영: 아이콘 오른쪽 에셋 여백 제거, Spacer로 글자와의 간격 부여 | 사용자 확인(실기기) |
| 2026-11-01 | 소수점 값을 정수로 정리 (아이콘 27x21dp, 간격 10dp, viewportWidth 27) | 사용자 요청 |
| 2026-11-01 | 작업 3~5 완료 표기, 작업 6 제외(사용처가 `NewPlanButton`뿐) | 빌드/테스트 결과, 실기기 확인 |
| 2026-11-02 | 디자인 피드백 반영: 전체 반전 방식 폐기, 전체 비반전으로 변경(FR-3 추가). 작업 1 제외, 작업 7(`autoMirrored` 제거) 추가, 작업 2~5 재검증 필요 표기 | 요구사항 문서(2026-11-01 댓글), 사용자 결정 |
