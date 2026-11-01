# 계획: New button icon RTL compatible (#382)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/382
- 요구사항: docs/spec/issue382/requirements.md
- 생성일: 2026-10-31
- 최종 수정일: 2026-10-31

## 맥락 및 목표
- "새로운 플랜" 버튼의 아이콘(`ic_new_plan`)이 RTL 레이아웃에서도 LTR과 동등하게 자연스럽게 보이도록 한다.
- 디자이너가 퇴사했으므로 에셋 수정도 개발자가 직접 한다.
- 아이콘은 팩맨 입이 오른쪽의 `+`를 향하는 구조다. RTL에서는 텍스트 오른쪽에 아이콘이 놓이므로, 아이콘 전체를 좌우 반전해 `+`가 왼쪽, 입이 왼쪽을 향하게 한다.

## 요구사항
- FR-1: RTL 환경에서 "새로운 플랜" 버튼 아이콘이 호환되어야 한다.
- FR-2: 이미지 교체가 필요하면 개발자가 직접 에셋을 제작한다.
- LTR 표시는 변경되지 않아야 한다.

## 아키텍처/설계
- `ic_new_plan`은 벡터 드로어블(`core/designsystem/src/main/res/drawable/ic_new_plan.xml`)이므로 래스터 에셋 교체는 필요 없다.
- 벡터에 `android:autoMirrored="true"`를 지정해 Compose `painterResource` / `Icon`이 레이아웃 방향에 따라 자동으로 반전하도록 한다. 별도의 RTL용 리소스(`drawable-ldrtl`)는 만들지 않는다.
- 에셋의 오른쪽 투명 여백(뷰포트 37 중 그림은 약 27까지)이 RTL 반전 시 바깥쪽 여백이 되므로, 뷰포트를 그림에 맞게 잘라(`viewportWidth=27`) 여백을 제거하고 `NewPlanButton`에서 `Spacer(10.dp)`로 아이콘과 글자 사이 간격을 준다. `Row`가 레이아웃 방향에 따라 자동으로 뒤집히므로 LTR/RTL 모두 글자 쪽에 간격이 생긴다.
- 사용처는 `HomeScreen.kt`의 `NewPlanButton`과 `IconButton.kt`이며, 둘 다 같은 드로어블을 쓰므로 에셋 한 곳의 수정으로 모두 적용된다.
- 가정: RTL 스크린샷은 디자인 확인용 가이드 화면이며 목표는 "아이콘 위치와 방향이 모두 RTL에 맞는 것"이다. (요구사항 문서의 미해결 질문 참고)

## 작업
1. `ic_new_plan.xml`에 `android:autoMirrored="true"` 추가.
   - **검증**: `git diff core/designsystem/src/main/res/drawable/ic_new_plan.xml`에서 속성 한 줄만 추가되었는지 확인. (완료)
2. `NewPlanButton`의 Compose Preview에 LTR/RTL 변형(`LocalLayoutDirection = Rtl`)을 추가해 반전 결과를 시각적으로 확인할 수 있게 한다. (완료: `NewPlanButtonPreview`, `NewPlanButtonRtlPreview`)
   - **검증**: Android Studio Preview에서 LTR은 기존과 같고 RTL은 아이콘이 오른쪽에 놓이며 좌우 반전되어 있는지 확인.
3. JDK 21 환경에서 빌드한다.
   - **검증**: `./gradlew :core:designsystem:assembleDebug :feature:home:assembleDebug`가 성공. (완료: `:app:assembleDebug` 성공)
4. 기존 테스트가 깨지지 않았는지 확인한다.
   - **검증**: `./gradlew :feature:home:test :core:androidx:compose:test`가 성공. (완료: `testDebugUnitTest` 성공)
5. 에뮬레이터/기기에서 RTL을 강제로 켜 실제 화면을 확인한다. (개발자 옵션의 "RTL 레이아웃 방향 강제 적용")
   - **검증**: 홈 화면 "새로운 플랜" 버튼이 RTL에서 텍스트 오른쪽에 아이콘이 있고 아이콘이 반전되어 있다. LTR 복귀 시 원래 모습이다. (완료: 실기기에서 확인)
~~6.~~ (Dropped: `ic_new_plan`은 `NewPlanButton`에서만 사용되며, `IconButton.kt`는 Preview에서만 참조한다.)

## 검증
- 코드: 작업 3, 4의 Gradle 명령이 성공한다.
- 화면: 작업 2, 5에서 LTR 불변, RTL 반전을 확인한다.
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
