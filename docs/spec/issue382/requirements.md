# 요구사항 분석: New button icon RTL compatible (#382)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/382
- 부모 이슈: 없음
- 라벨: Design
- 생성일: 2025-01-21
- 최종 수정일: 2026-10-31
- 분석 기준점: 2025-10-27T14:30:52Z

## 배경 및 문제
- 이슈 제목은 "New button icon RTL compatible"이며, 본문에는 LTR 화면과 RTL 화면의 스크린샷 두 장만 첨부되어 있다.
- LTR 스크린샷: "새로운 플랜" 버튼에서 아이콘(+ 표시가 붙은 아이콘)이 텍스트 왼쪽에 위치한다.
- RTL 스크린샷: 같은 버튼에서 아이콘이 텍스트 오른쪽에 위치하며, 레이아웃 가이드 선이 표시된 디자인 확인용 화면이다.
- 본문에 문제 설명 문장은 없다. 제목과 스크린샷으로 볼 때, "새로운 플랜" 버튼의 아이콘을 RTL 환경에 맞게 대응하는 디자인 작업으로 읽힌다.
- 유일한 댓글(2025-10-27, 소유자)은 특정 담당자에게 작업을 부탁하는 내용이며, 요구사항 정보는 없다.

## 목표
- "새로운 플랜" 버튼 아이콘이 RTL 레이아웃에서도 LTR과 동등하게 자연스럽게 보이도록 한다. (제목 기준)

## 범위
### 포함
- "새로운 플랜" 버튼의 아이콘 (제목의 "New button icon").

- 디자이너가 퇴사했으므로, 필요한 아이콘 에셋 수정도 개발자가 직접 수행한다. (사용자 전달 사항)

### 제외
- 본문과 댓글에 명시된 제외 항목 없음.

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | "새로운 플랜" 버튼의 아이콘이 RTL 환경에서 호환되어야 한다. | 제목, 본문(스크린샷) |
| FR-2 | 아이콘 이미지 교체가 필요하면 개발자가 직접 에셋을 제작한다. | 사용자 전달 사항 (디자이너 퇴사) |

## 비기능 요구사항
N/A

## 인수 조건
- [ ] RTL 레이아웃에서 "새로운 플랜" 버튼 아이콘이 RTL에 맞게 표시된다. (구체적 기준은 Open Questions 참고)
- [ ] LTR 레이아웃의 기존 표시는 변경되지 않는다. (LTR 스크린샷 첨부 근거)

## 영향 범위
코드에서 확인한 "새로운 플랜" 관련 위치:
- 아이콘 리소스: `core/designsystem/src/main/res/drawable/ic_new_plan.xml` (벡터 드로어블, 래스터 이미지 아님)
- `feature/home/src/main/kotlin/com/nlab/reminder/feature/home/ui/HomeScreen.kt` (`NewPlanButton`, 아이콘과 `StringIds.new_plan` 텍스트를 함께 사용)
- `core/androidx/compose/src/main/kotlin/com/nlab/reminder/core/androidx/compose/ui/IconButton.kt` (Preview에서만 `ic_new_plan` 참조)
- `core/component/schedulelist/.../FormViewHolderDelegate.kt` (`StringIds.new_plan` 문자열만 사용)
- 문자열: `core/translation/src/main/res/values/strings.xml` (`new_plan`)
- 앱 매니페스트는 `android:supportsRtl="true"`로 RTL을 이미 지원한다.

## 미해결 질문
- RTL 스크린샷이 현재 문제 상태인지 목표 디자인인지는 확인되지 않았다.

## 충돌 및 참고
- 본문과 댓글 사이에 충돌은 없다. 댓글은 요구사항 정보를 담고 있지 않다.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-10-31 | 최초 작성 | 본문, 댓글 |
| 2026-10-31 | 디자이너 퇴사로 에셋 직접 제작 요구(FR-2) 추가, 담당자 관련 미해결 질문 해소, 아이콘이 벡터 드로어블임을 영향 범위에 반영 | 사용자 전달 사항 |
| 2026-11-01 | 미해결 질문 정리: 반전 방식은 구현으로 결정, 대상은 `NewPlanButton`뿐임을 코드로 확인 | 코드 확인, 사용자 확인 |
