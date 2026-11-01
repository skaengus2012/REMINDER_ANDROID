# 계획: [Task] Integrate Flocon (#875)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/875
- 요구사항: docs/spec/issue875/requirements.md
- 작성일: 2026-11-01
- 최종 수정일: 2026-11-01

## 맥락/목표
네트워크 요청을 디버깅하기 쉽게 하려고 Flocon을 도입한다. 디버그 빌드에서만 동작해야 하며, Flocon 앱에서 요청과 응답을 확인할 수 있어야 한다.

현재 네트워크 요청 경로는 두 가지다.
- Coil `ImageLoader`: `core/network`의 `NetworkModule`에서 제공하며, Coil 2.x가 내부적으로 OkHttp를 사용한다.
- Jsoup `Jsoup.connect(...)`: `LinkThumbnailDataSourceImpl`에서 사용하며, `HttpURLConnection` 기반이라 OkHttp 인터셉터로 잡히지 않는다.

`OkHttpClient`를 하나만 만들어 DI로 전파하고, 인터셉터는 멀티바인딩(`Set<Interceptor>`)으로 주입한다. 이렇게 하면 Flocon 인터셉터를 debug 소스셋에서만 등록할 수 있다. 구조는 android-boot 프로젝트(`AppScopeModule`, `DebugAppScopeProvideModule`, `FloconInitializer`)와 nowinandroid(`NetworkModule`)를 참고한다.
- android-boot: `Set<Interceptor>` 멀티바인딩과 `src/debug` 소스셋으로 디버그 전용 인터셉터를 주입한다. Flocon은 `debugImplementation`이라 release에 클래스가 없으므로, nowinandroid의 `BuildConfig.DEBUG` 분기 방식은 쓰지 않는다.
- nowinandroid: 단일 `OkHttpClient`를 `Call.Factory`로 노출하고, `ImageLoader`가 `.callFactory { ... }`와 `dagger.Lazy`로 가져다 쓴다. 클라이언트는 `OkHttpClient`로 노출하기로 했다(구현 시 결정).

## 요구사항
`requirements.md`의 FR-1~3, NFR-1을 따른다.

- FR-1: 네트워크 요청을 수행하는 모든 모듈에 Flocon을 통합한다. 공유 `OkHttpClient`를 통해 Coil과 링크 썸네일 요청이 모두 대상이 된다.
- FR-2, NFR-1: Flocon은 debug 빌드에만 포함한다. 의존성은 `debugImplementation`, 코드는 `src/debug`에 둔다.
- FR-3: Flocon 앱에서 요청과 응답을 확인할 수 있다.

범위 결정 (사용자 합의, 2026-11-01):
- 단일 `OkHttpClient`를 만들어 전파한다.
- `TrustedTimeDataSource`는 Play Services Time API를 쓰며 OkHttp 경로가 아니므로 대상에서 제외한다.

## 아키텍처/설계
1. **`OkHttpClient` 제공 (`core/network`)**
   - `NetworkModule`에 `@Singleton OkHttpClient`를 추가한다. `Set<@JvmSuppressWildcards Interceptor>`를 받아 `addInterceptor`로 등록한다.
   - `@Multibinds`로 빈 `Set<Interceptor>`를 선언해, release에서도 바인딩이 비어 있는 채로 컴파일되게 한다.
   - `ImageLoader`는 `.okHttpClient { okHttpClientProvider.get() }`(`dagger.Lazy`)로 공유 클라이언트를 사용한다.
2. **링크 썸네일 요청 OkHttp 전환 (`core/network`)**
   - Retrofit 인터페이스 `LinkThumbnailApi`(`@Streaming @GET suspend fun getHtml(@Url url: String): ResponseBody`)로 HTML을 받고, 응답 본문 스트림을 Jsoup streamParser로 파싱하도록 바꾼다. `LinkThumbnailDataSourceImpl`은 `OkHttpClient`를 직접 받지 않고 이 인터페이스만 주입받는다.
   - `OkHttpClient`는 `NetworkModule` 안에서만 `dagger.Lazy`로 쓴다(`ImageLoader`, `Retrofit`의 `callFactory`). 데이터소스가 클라이언트를 직접 받아 `Lazy` 없이 쓰는 실수를 막기 위함이다.
   - `@Streaming`이 없으면 Retrofit이 본문 전체를 버퍼링하므로 반드시 붙인다.
   - 기존 동작(`og:title`, `og:image` 추출, `catching` 기반 `Result`)은 유지한다. 동작 보존은 기존 테스트로 확인한다.
3. **Flocon 도입 (`app`, debug 전용)**
   - 버전 카탈로그에 `flocon`, `flocon-okhttp`(참고 프로젝트 기준 1.10.0), `okhttp`(4.12.0), `retrofit`(3.0.0)을 추가한다. `core/network`가 `OkHttpClient`를 직접 사용하므로 `okhttp`는 명시적으로 선언한다.
   - `app/build.gradle.kts`에 `debugImplementation(libs.flocon)`과 `debugImplementation(libs.flocon.okhttp)`를 추가한다.
   - `app/src/debug`에 `FloconInitializer`(`Flocon.initialize(context)`)를 만들고 debug `AndroidManifest.xml`의 `InitializationProvider`에 등록한다.
   - `app/src/debug`에 `@IntoSet Interceptor`로 `FloconOkhttpInterceptor()`를 제공하는 Hilt 모듈(`AppScopeDebugApplicationModule`, 기존 `AppScope<이름>Module` 관례)을 추가한다.

## 작업
1. 버전 카탈로그에 `okhttp`, `retrofit`, `flocon`, `flocon-okhttp`를 추가하고 `core/network`에 `okhttp`, `retrofit` 의존성을 선언한다.
   - **Verify**: `./gradlew :core:network:assembleDebug`
2. `NetworkModule`에 `Set<Interceptor>` `@Multibinds`와 `@Singleton OkHttpClient`를 추가하고, `ImageLoader`가 이 클라이언트를 사용하게 한다.
   - **Verify**: `./gradlew :app:assembleDebug` (Hilt 그래프 컴파일 확인)
3. `LinkThumbnailDataSourceImpl`이 `LinkThumbnailApi`(Retrofit)를 주입받아 요청하고 Jsoup으로 파싱하게 바꾼다. `NetworkModule`/`NetworkDataSourceModule`의 제공 코드를 함께 수정한다.
   - 모든 경우에 `StreamParser`로 스트리밍 파싱한다. 서버가 charset을 선언하지 않으면 본문 앞 1KB의 `<meta>`에서 charset을 감지한다.
   - 브라우저 UA는 지정하지 않는다(비교 결과 이점이 없었고, 일부 사이트는 OkHttp 기본 UA에서만 og 태그를 제공).
   - 코루틴 취소 시 호출 취소는 Retrofit의 `suspend` 지원이 처리한다(직접 만든 `Call.await()`는 제거).
   - **Verify**: `./gradlew :core:network:test :core:data-impl:test`
4. ~~링크 썸네일 데이터소스 테스트를 추가한다.~~ (Dropped: 리뷰 결과 네트워크 동작에 대한 테스트는 불필요하다고 판단해 테스트와 `testImplementation` 의존성을 제거했다.) 원래 내용: 링크 썸네일 데이터소스 테스트를 추가한다. `MockWebServer` 또는 가짜 `Interceptor`로 `og:title`/`og:image` 추출, 메타 태그 누락, 요청 실패 시 `Result` 실패를 검증한다. (프로젝트 TDD 원칙에 따라 3번보다 먼저 작성한다.)
   - **Verify**: `./gradlew :core:network:test`
5. `app`에 Flocon 의존성을 `debugImplementation`으로 추가하고, debug 소스셋에 `FloconInitializer`와 Flocon 인터셉터 Hilt 모듈을 추가한다. debug `AndroidManifest.xml`에 Initializer를 등록한다.
   - **Verify**: `./gradlew :app:assembleDebug`
6. release 빌드에 Flocon이 포함되지 않는지 확인한다.
   - **Verify**: `./gradlew :app:assembleRelease` 성공, 그리고 `./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep -i flocon`의 결과가 비어 있음
7. 전체 단위 테스트를 실행한다.
   - **Verify**: `./gradlew test`
8. Flocon 앱을 연결한 상태에서 debug 앱을 실행해 요청/응답 확인과 앱 동작을 점검한다.
   - **Verify**: Flocon 앱에서 이미지 요청과 링크 썸네일 요청의 요청/응답이 보인다. 링크 입력 시 썸네일이 정상 표시된다. (수동 확인)

## 검증
- 빌드: `./gradlew :app:assembleDebug`, `./gradlew :app:assembleRelease`
- 테스트: `./gradlew test`
- release 제외 확인: `releaseRuntimeClasspath`에 Flocon 의존성이 없다 (NFR-1, FR-2).
- 수동 확인: Flocon 앱에서 Coil 이미지 요청과 링크 썸네일 요청이 모두 확인된다 (FR-1, FR-3).

## 미결 사항
- Flocon 버전(1.10.0)은 참고 프로젝트 기준이므로 구현 시 최신 안정 버전을 확인한다.
- `TrustedTimeDataSource`(Play Services)는 Flocon 대상에서 제외하는 것으로 계획했다. 포함이 필요하면 알려 달라.
- 링크 썸네일 요청은 현재 앱 UI에서 호출되지 않는다(`feature/all`이 `FakeData`를 사용). 따라서 Task 8에서 Flocon으로 확인된 것은 Coil 이미지 요청뿐이며, 썸네일 요청은 확인하지 못했고 테스트도 두지 않았다. 화면 연결(#601) 이후 재확인이 필요하다.
- 코드 리뷰(자동)에서 지적됐으나 반영하지 않은 항목: OkHttp 기본 타임아웃(10초, 기존 Jsoup 30초), 이미지/썸네일 클라이언트 분리, 인터셉터 실행 순서 보장.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-01 | 최초 작성 | requirements.md, 사용자 요청 (단일 OkHttpClient 전파 방향) |
| 2026-11-01 | 참고 구조에 nowinandroid 추가, 절대 경로 제거 | 사용자 요청 |
| 2026-11-01 | Task 1~7 구현 완료. 클라이언트 노출 타입을 `OkHttpClient`로 확정. Task 8(수동 확인)은 미수행 | 구현 |
| 2026-11-01 | 코드 리뷰 반영: 브라우저 UA, charset 감지, 취소 전파. 실기기 확인 결과와 미반영 항목을 미결 사항에 기록 | 코드 리뷰, 실기기 확인 |
| 2026-11-01 | PR 리뷰 반영: 전 경로 스트리밍 파싱(meta charset 감지), 브라우저 UA 제거, 모듈명을 `AppScopeDebugApplicationModule`로 변경, Task 4(테스트) 제외 | PR #881 리뷰 코멘트 |
| 2026-11-01 | PR 리뷰 반영: OkHttp 5 상향 대신 Retrofit 3.0.0(OkHttp 4.12 유지)으로 링크 썸네일 요청을 구현. 데이터소스가 `OkHttpClient`를 직접 받지 않도록 `LinkThumbnailApi`만 주입 | PR #881 리뷰 코멘트, 사용자 결정 |
