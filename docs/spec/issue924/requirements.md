# 요구사항 분석: [Task] Migrate to Flocon 2.0.0 (recover closed dependabot PR #922) (#924)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/924
- 부모 이슈: 없음
- 라벨: dependencies
- 생성일: 2026-11-16
- 최종 수정일: 2026-11-17
- 분석 기준점: 2026-11-16T16:49:14Z

## 배경 및 문제
- Dependabot PR #922(`Bump flocon from 1.10.0 to 2.0.0`)가 `Build check (debug)`에서 실패했고, dependabot ignore 코멘트와 함께 닫혔다. 따라서 2.0.0 업그레이드는 다시 제안되지 않는다.
- Flocon 2.0은 Kotlin SDK에 breaking change를 포함한다.
  - `Flocon.initialize(context)`가 제거되고 `startFlocon { install(...) }` DSL로 대체되었다.
  - SDK가 기능별 모듈로 분리되었다.
- 그 결과 `FloconInitializer`가 `Unresolved reference 'initialize'`로 컴파일에 실패한다.
- 이슈에는 댓글이 없다.

## 목표
- Flocon을 2.0.0으로 올리고, 디버그 빌드와 릴리스 빌드 체크가 통과하도록 마이그레이션한다.

## 범위
### 포함
- `gradle/libs.versions.toml`의 `flocon` 버전 상향.
- 디버그 빌드용 `flocon-network-core` 모듈 추가.
- `FloconInitializer`의 초기화 코드 교체.

### 제외
- 본문에 명시된 제외 항목 없음.

## 기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| FR-1 | `gradle/libs.versions.toml`에서 `flocon` 버전을 2.0.0으로 올린다. | 본문 (Needs) |
| FR-2 | 디버그 빌드에 `flocon`, `flocon-okhttp-interceptor`와 함께 `flocon-network-core` 모듈을 추가한다. | 본문 (Needs) |
| FR-3 | `FloconInitializer`의 `Flocon.initialize(context)`를 `startFlocon(context) { install(FloconNetwork) }`로 교체한다. | 본문 (Needs) |

## 비기능 요구사항
| ID | 요구사항 | 출처 |
|----|----------|------|
| NFR-1 | `:app:assembleDebug`가 성공해야 한다. | 본문 (Needs) |
| NFR-2 | 릴리스 빌드 체크가 통과해야 한다. | 본문 (Needs) |

## 인수 조건
- [ ] `gradle/libs.versions.toml`의 `flocon` 버전이 2.0.0이다.
- [ ] 디버그 빌드에 `flocon-network-core`가 `flocon`, `flocon-okhttp-interceptor`와 함께 포함된다.
- [ ] `FloconInitializer`가 `startFlocon(context) { install(FloconNetwork) }`를 사용한다.
- [ ] `./gradlew :app:assembleDebug`가 통과한다.
- [ ] 릴리스 빌드 체크(CI)가 통과한다.

## 영향 범위
코드에서 확인한 내용만 적는다.
- `gradle/libs.versions.toml`: `flocon = "1.10.0"`(32행), `flocon`, `flocon-okhttp` 라이브러리 정의(89~90행).
- `app/build.gradle.kts`: `debugImplementation(libs.flocon)`, `debugImplementation(libs.flocon.okhttp)`(94~95행).
- `app/src/debug/kotlin/com/nlab/reminder/apps/startup/init/FloconInitializer.kt`: `Flocon.initialize(context)` 호출.
- `app/src/debug/kotlin/com/nlab/reminder/apps/di/DebugAppsModule.kt`: `FloconOkhttpInterceptor()` 사용(`io.github.openflocon.flocon.okhttp`).
- `app/src/debug/AndroidManifest.xml`: `FloconInitializer` startup 등록.
- `.github/workflows/ci.yml`: `Build check` 잡에서 `./gradlew assembleDebug` 실행.
- 관련 이력 문서: `docs/spec/issue875/` (Flocon 최초 도입 이슈).

## 미결정 사항 (Open Questions)
- `startFlocon`, `FloconNetwork`의 패키지(import 경로)와 Flocon 2.0.0에서 `flocon-okhttp-interceptor`의 패키지/클래스명(`FloconOkhttpInterceptor`)이 유지되는지 확인이 필요하다. 이슈는 해당 내용을 다루지 않는다.
- `flocon-network-core`의 Maven 좌표(group:artifact)와 버전 카탈로그 alias 이름을 결정해야 한다. 이슈는 모듈명만 제시하고 좌표를 명시하지 않는다.
- `flocon-network-core`를 `libs.versions.toml`에서 기존 `flocon` 버전(version.ref)과 공유할지 결정해야 한다.
- "release build check"가 무엇을 가리키는지 불명확하다. CI의 `Build check`(release 변형 포함 여부)인지, 별도 `assembleRelease` 실행인지, 릴리스 빌드에 Flocon이 포함되지 않아야 한다는 #875의 조건(`docs/spec/issue875`)을 함께 확인하는지 결정이 필요하다. (현재 `ci.yml`은 `assembleDebug`만 확인했다.)
- Flocon 2.0의 신규 기능 모듈(network 외 database, SharedPreferences 등) 중 `FloconNetwork` 외에 `install`할 대상이 있는지 결정이 필요하다. 이슈는 `FloconNetwork`만 명시한다.
- 2.0.0 업그레이드에 따라 Flocon 데스크톱 앱 버전도 맞춰야 하는지(호환성), 그리고 이를 문서화해야 하는지 결정이 필요하다.
- 테스트 요구사항: 디버그 전용 초기화 코드라서 단위 테스트 대상이 아닌지, 수동/빌드 검증만으로 충분한지 결정이 필요하다.

## 충돌 및 참고
- 본문과 댓글 사이에 충돌 없음(댓글이 없음).
- 이슈 본문은 영어이나, 사용자 전역 규칙과 기존 spec 문서 관례에 따라 한국어로 작성했다.
- 부모 이슈 조회 결과 부모가 없어 추가로 읽은 이슈는 없다.
- 이슈의 `startFlocon(context) { install(FloconNetwork) }` 표기는 `Needs` 항목 기준이며, `Problem` 항목은 `startFlocon { install(...) }`로 표기해 context 인자 유무가 다르게 보인다. 본문에서 더 구체적인 `Needs`의 표기를 채택했다. 실제 시그니처는 위 Open Questions의 확인 대상이다.

---

## 변경 이력
| 날짜 | 변경 | 출처 |
|------|------|------|
| 2026-11-17 | 최초 작성 | 본문 |
