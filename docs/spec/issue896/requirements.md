# 요구사항 분석: [Task] Optimize home blur layer rendering (#896)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/896
- 부모 이슈: #895 ([Task] Optimize Home screen)
- 라벨: Compose, Performance
- 생성일: 2026-11-01
- 최종 수정일: 2026-11-03
- 분석 기준점: 2026-11-01T17:53:51Z

## 배경 및 문제
- #893의 후속 이슈다. PR #894를 Jetpack Compose 성능 체크리스트로 점검하다가 발견되었다.
- `BottomBlurLayer`가 `drawBehind` 안에서 `bodyScrollState.value`를 직접 읽는다. 알파가 상수(0 또는 1)인 구간에서도 스크롤 프레임마다 레이어가 다시 그려진다. #893 이전에는 `derivedStateOf` 결과를 읽어서 알파가 실제로 바뀔 때만 다시 그려졌다.
- `HeadBlurLayer`는 `Box` 1개와 `Spacer` 2개로 구성된다. 장식용 노드이므로 `BottomBlurLayer`처럼 `drawBehind` 노드 1개로 합칠 수 있다.
- 0.5dp 선을 `BottomBlurLayer`는 소수 px 높이의 `drawRect`로 그리고, `HeadBlurLayer`는 반올림되는 `Spacer.height(0.5.dp)`로 그린다. 두 레이어의 선 두께가 달라질 수 있다 (#894 리뷰 제안 F1).
- 부모 이슈 #895는 Home 화면의 Compose 성능/렌더링 이슈를 하위 이슈로 나누어 추적하며, 하위 이슈에서 달리 말하지 않는 한 기존 UI 동작을 유지하라고 한다.
- 이슈에 댓글은 없다.

## 목표
- Home 블러 레이어(`BottomBlurLayer`, `HeadBlurLayer`)의 스크롤 중 불필요한 재그리기와 노드 수를 줄인다.
- 시각적 결과는 현재와 동일하게 유지하고, 효과를 벤치마크로 확인한다.

## 범위
### 포함
- `BottomBlurLayer`: 정적 rect는 한 번만 그리고 알파만 `Modifier.graphicsLayer` + `CompositingStrategy.ModulateAlpha`로 갱신
- `HeadBlurLayer`: 모든 사용처 확인 후 `drawBehind` 노드 1개로 통합
- 두 레이어의 0.5dp 선 그리기 방식 통일
- (선택) `HomeStatusBackground`의 알파 상태에 대한 `derivedStateOf` vs `computedStateOf` 비교
- 변경 전/후 벤치마크 측정

### 제외
- 시각적 결과의 변경
- 이슈에 언급되지 않은 다른 Home 화면 성능 이슈 (부모 #895의 다른 하위 이슈에서 다룸)

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `BottomBlurLayer`는 정적 rect를 한 번만 그리고, 알파만 `Modifier.graphicsLayer`와 `CompositingStrategy.ModulateAlpha`로 갱신한다. 현재의 draw op별 알파(컨테이너 0.925, 선 1.0)를 유지한다. | 본문 (Needs) |
| FR-2 | `HeadBlurLayer`를 사용처(`ScheduleListToolbar`, `HomeStatusBackground`)를 모두 확인한 뒤 하나의 `drawBehind` 노드로 통합한다. | 본문 (Needs) |
| FR-3 | 0.5dp 선을 `BottomBlurLayer`와 `HeadBlurLayer`에서 같은 방식으로 그린다. | 본문 (Needs) |
| FR-4 | (선택) `HomeStatusBackground`의 알파 상태에 `derivedStateOf`와 `computedStateOf`를 비교한다. | 본문 (Needs, Optional) |

## 비기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| NFR-1 | 렌더링 결과가 시각적으로 현재와 동일해야 한다. | 본문 (Needs), 부모 이슈 |
| NFR-2 | 변경 전/후를 벤치마크(예: Macrobenchmark)로 측정해 효과를 확인한다. | 본문 (Needs) |

## 인수 조건
- [ ] `BottomBlurLayer`의 정적 rect가 스크롤 중 다시 그려지지 않고, 알파만 `graphicsLayer`(`ModulateAlpha`)로 갱신된다.
- [ ] `BottomBlurLayer`의 컨테이너 알파 0.925와 선 알파 1.0이 유지된다.
- [ ] `HeadBlurLayer`가 `drawBehind` 노드 1개로 통합되고, `ScheduleListToolbar`와 `HomeStatusBackground` 양쪽 사용처가 이전과 동일하게 보인다.
- [ ] 두 레이어의 0.5dp 선이 같은 방식으로 그려져 두께가 일치한다.
- [ ] 변경 전/후 시각적 차이가 없다.
- [ ] 벤치마크로 변경 전/후 효과가 확인되고 결과가 기록된다.
- [ ] (선택) `derivedStateOf`와 `computedStateOf` 비교 결과가 기록된다.

## 영향 범위
코드에서 확인한 내용:
- `core/androidx/compose/src/main/kotlin/com/nlab/reminder/core/androidx/compose/ui/HeadBlurLayer.kt`: 현재 `Box` + `Spacer` 2개 구조 (`alpha`, `containerColor`, `lineColor`, `defaultContainerAlpha` 파라미터)
- `core/component/schedulelist/.../toolbar/ui/ScheduleListToolbar.kt`: `HeadBlurLayer` 사용 (`containerColor = bg2Layer`)
- `feature/home/.../ui/HomeScreen.kt`: `HomeStatusBackground`(`HeadBlurLayer` 사용, `derivedStateOf`로 알파 계산), `BottomActions`, `BottomBlurLayer`
- `master` 기준 `settings.gradle.kts`, `gradle/libs.versions.toml`에는 benchmark/Macrobenchmark 관련 모듈 또는 의존성이 보이지 않는다.
- `origin/feature/issue893` 브랜치(PR #894)에 #893 변경이 있으며, `HeadBlurLayer`의 alpha 파라미터가 람다로 바뀌고 `BottomBlurLayer`가 `drawBehind`에서 스크롤 상태를 읽는다. 이 브랜치의 변경은 현재 작업 트리(master 기반)에 없다.

## 미해결 질문
- 선행 작업: 본문의 "현재 상태"(`drawBehind` 안에서 `bodyScrollState.value` 직접 읽기, `HeadBlurLayer`의 alpha 람다)는 PR #894 (`feature/issue893`)에 있고 현재 master에는 없다. #894가 master에 머지된 뒤 시작하는가, 아니면 `feature/issue893` 위에서 작업하는가? 이에 따라 base 브랜치가 달라진다.
- 벤치마크 환경: 저장소에 Macrobenchmark 모듈이 없다. 모듈을 새로 추가하는 것이 이 이슈의 범위인가? 아니면 일회성 로컬 측정(결과만 PR에 기록)으로 충분한가? 측정 대상 기기와 시나리오(예: Home 화면 스크롤, 측정 지표 FrameTimingMetric 등)도 정해야 한다.
- "시각적으로 동일"의 확인 방법: 기기 육안 확인, 스크린샷 비교, 테스트 중 무엇으로 확인하는가? 0.5dp 선 통일(FR-3) 시 한쪽의 선 두께가 달라질 수 있으므로, 어느 쪽 방식(소수 px `drawRect`, 반올림 등)에 맞추는가?
- FR-3의 통일 기준: 이슈는 "같은 방식"이라고만 하고 어느 쪽에 맞출지 명시하지 않는다. 본문 맥락상 `BottomBlurLayer`의 `drawRect` 방식으로 보이나 확정이 필요하다.
- `HeadBlurLayer`의 공개 API(파라미터, 특히 `alpha`를 값으로 받을지 람다로 받을지, `defaultContainerAlpha`)를 변경해도 되는가? #893 이후 시그니처가 바뀌어 있어, 기준 브랜치에 따라 달라진다.
- FR-4는 선택 사항이다. 이번 PR에 포함하는가? 포함한다면 `computedStateOf`가 현재 사용 중인 Compose 버전에서 제공되는지 확인이 필요하다 (저장소에 사용 흔적 없음). 비교 결과 채택 기준(성능 수치, 가독성 등)은 무엇인가?
- 부모 이슈의 하위 이슈 중 같은 파일(`HomeScreen.kt`, `HeadBlurLayer.kt`)을 다루는 이슈와 PR이 겹칠 경우 병합 순서를 어떻게 하는가?

## 충돌 및 참고
- 본문과 댓글 간 충돌은 없다 (댓글 없음).
- 본문은 `drawBehind`에서 `bodyScrollState.value`를 직접 읽는 상태를 전제로 하지만, 현재 작업 트리의 `BottomBlurLayer`는 아직 `derivedStateOf` + `onSizeChanged` 구조다. 전제는 PR #894 기준이다 (미해결 질문 참고).

- PR #894 정적 분석 노트: 블러 알파 계산(`BottomBlurLayer`, `HomeStatusBackground`)은 애니메이션 구간 길이가 0이면 `0 / 0f = NaN`이 된다. 현재는 호출부가 상수이고 `private`이라 도달하지 않지만, 이번 이슈에서 `BottomBlurLayer` 알파 계산을 `graphicsLayer`로 옮기므로 함께 방어한다(동작 변화 없음). 상세는 `plan.md`의 "0 나누기(NaN) 방어" 참고.

---

## 변경 이력
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-03 | 최초 작성 | 본문, 부모 이슈 #895, 관련 이슈 #893 |
| 2026-11-03 | 0 나누기(NaN) 방어 참고 사항 추가 | PR #894 정적 분석 노트 |
