# Plan: [Refactor] Make TagEditTask.processAsFlow an explicit dependency instead of a top-level extension function (#910)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/910
- Requirements: docs/spec/issue910/requirements.md
- Created: 2026-11-09
- Last Updated: 2026-11-09

## Context/Goals
`TagEditTask.processAsFlow()` is a public top-level extension function. `HomeReduce` calls it at 5 places, so `HomeReduceKtTest` must use `mockkStatic(TagEditTask::processAsFlow)`, which hides the dependency and makes the tests fragile.
Goal: make it an explicit, injectable dependency held by `HomeEnvironment`, remove the extension function, and drop the static mocking from caller tests, without changing behavior.

## Requirements
- FR-1: `processAsFlow` is provided as an explicit injectable dependency (not a top-level extension).
- FR-2: `HomeReduce` receives it explicitly (through `HomeEnvironment`).
- FR-3: `HomeReduceKtTest` has no `mockkStatic`/`unmockkStatic` of `processAsFlow`.
- NFR-1/2: Behavior and test coverage of the processing logic are unchanged (the same 4 scenarios are still tested).
- Approved decisions: dependency lives in `HomeEnvironment`; the extension function is removed; only `HomeReduce` is a call site; `TagEditTaskKtTest` is restructured to fit the chosen form.

## Architecture/Design
### Form: single concrete `UseCase` class
```kotlin
// core/component/tag, package ...tag.edit
@Reusable
class GetTagEditTaskTransitionFlowUseCase @Inject constructor() {
    operator fun invoke(task: TagEditTask): Flow<Result<TagEditStateTransition>> = flow { /* logic moved verbatim */ }
}
```
Rationale:
- The existing UseCase classes in `core/component` (`GetCurrentTimeSnapshotStreamUseCase`, `GetScheduleListResourcesFlowUseCase`, `GetUserScheduleListResourcesFlowUseCase`) are concrete classes with `operator fun invoke`, with no interface. A UseCase is a single-action, stateless unit, so an interface/`Impl` pair adds nothing. This also avoids a `@Binds` module.
- Hilt can inject it directly with `@Inject constructor()`; a function type would be an anonymous generic `Function1<...>` binding, so a named class is preferred.
- The KDoc of the old `processAsFlow` moves onto this class.
- The class stays final (public, so `feature:home` can reference it); only the logic is exposed through `invoke`.

### Naming
Source: Android developer guide "Domain layer", Naming conventions (https://developer.android.com/topic/architecture/domain-layer#conventions). Use cases are named after the single action they are responsible for: *verb in present tense* + *noun/what (optional)* + *UseCase* (examples: `FormatDateUseCase`, `LogOutUserUseCase`, `GetLatestNewsWithAuthorsUseCase`). The same document also recommends `operator fun invoke()` for calling a use case like a function, and states that use cases should not hold mutable data.
- Final name (decided by the user): `GetTagEditTaskTransitionFlowUseCase` = verb `Get` + what `TagEditTaskTransitionFlow` + `UseCase`.
- The use case does NOT execute the task. It returns a cold `Flow`, so nothing runs on invocation; `TagEditTask.processAndGet()` runs only when the flow is collected. Its role is to convert a `TagEditTask` into a cold flow of `Result<TagEditStateTransition>`. Therefore execution verbs (`Execute`/`Run`/`Process`) were rejected.
- `Get...Flow` matches the project convention (`GetScheduleListResourcesFlowUseCase`, `GetCurrentTimeSnapshotStreamUseCase`), and `Transition` names the flow's element type (`TagEditStateTransition`).
- No `-er` class names (user preference).
- Field naming: the repo drops the `UseCase` suffix for UseCase properties (e.g. `getScheduleListResourcesFlow: GetScheduleListResourcesFlowUseCase`), so the `HomeEnvironment` field is `getTagEditTaskTransitionFlow`.

### Hilt provision
- No `@Module` and no `@Binds`/`Impl`: `@Reusable` plus `@Inject constructor()` is enough. The class is stateless (as the guide requires), and `@Reusable` matches `TagEditStateMachine` in the same package and the `@Reusable` providers in other `AppScope...Module`s.
- Consumer is `HomeEnvironment` (`@HiltViewModel`), which can take `@Reusable`/unscoped bindings. The `tag` module already applies `nlab.android.library.di`, so no Gradle change is expected.

### Consumers
- `HomeEnvironment` gets a new constructor field `val getTagEditTaskTransitionFlow: GetTagEditTaskTransitionFlowUseCase` (concrete class).
- `HomeReduce`: replace `.processAsFlow()` with `environment.getTagEditTaskTransitionFlow(task)` at 5 places by chaining the state machine call into the use case (e.g. `environment.tagEditStateMachine.startRename(...).let(environment.getTagEditTaskTransitionFlow::invoke)`), remove the extension import.
- Remove `fun TagEditTask.processAsFlow()` from `TagEditTask.kt` and clean now-unused imports (`Flow`, `flow`).

### Tests
- `TagEditTaskKtTest`: keep the 3 constructor tests there; move the 4 `process as flow` tests to a new `GetTagEditTaskTransitionFlowUseCaseTest` (calls `GetTagEditTaskTransitionFlowUseCase()(task)`, naming like `GetCurrentTimeSnapshotStreamUseCaseTest`). Same scenarios and assertions (NFR-2).
- `HomeTestGenerator.genHomeEnvironment` gets a `getTagEditTaskTransitionFlow: GetTagEditTaskTransitionFlowUseCase = mockk()` parameter.
- `HomeReduceKtTest`: `mockkStatic`/`unmockkStatic` stay removed; `runTestWithTagEditTaskMock` becomes plain `runTest`. The use case is a mockk of the concrete class (`every { this@mockk(task) } returns flowOf(...)`, or `coEvery`/`every` on `invoke`) passed to `genHomeEnvironment` through private `genGetTagEditTaskTransitionFlowUseCase` helpers kept in `HomeReduceKtTest` (their only consumer, so they are not shared via `tag-test`); the task returned by the `tagEditStateMachine` mock is a plain `mockk<TagEditTask>()` matched by instance.
- Mocking a final Kotlin class: the project already does this. `HomeReduceKtTest`/`genHomeEnvironment` mock the final class `TagEditStateMachine` with `mockk()`, and `TagEditTask` itself is a final class mocked today. mockk 1.14.11 (`libs.versions.toml`, exposed through `testkit` as `api(libs.mockk)`) uses the inline mock maker on the JVM, so no `open`/`all-open` change is needed. Task 4 confirms it by running the tests.

## Tasks
1. Add the concrete class `GetTagEditTaskTransitionFlowUseCase` (`@Reusable @Inject constructor()`, `operator fun invoke`, KDoc and logic copied from the extension) in `core/component/tag`. Keep the extension for now so the build stays green.
   - **Verify**: `./gradlew :core:component:tag:compileDebugKotlin`
2. Add `GetTagEditTaskTransitionFlowUseCaseTest` with the 4 process-as-flow scenarios, and remove those 4 tests from `TagEditTaskKtTest`.
   - **Verify**: `./gradlew :core:component:tag:testDebugUnitTest`
3. Add `getTagEditTaskTransitionFlow` to `HomeEnvironment`, switch the 5 call sites in `HomeReduce` to it, and add the parameter (default `mockk()`) to `genHomeEnvironment`.
   - **Verify**: `./gradlew :feature:home:compileDebugKotlin :feature:home:compileDebugUnitTestKotlin`
4. Rewrite `HomeReduceKtTest` to inject a mockk of `GetTagEditTaskTransitionFlowUseCase` and delete all `mockkStatic`/`unmockkStatic` usage and the `processAsFlow` import.
   - **Verify**: `./gradlew :feature:home:testDebugUnitTest`, and `grep -rn "mockkStatic(TagEditTask" feature/` returns nothing.
5. Remove `fun TagEditTask.processAsFlow()` from `TagEditTask.kt` and clean unused imports.
   - **Verify**: `grep -rn "TagEditTask.processAsFlow\|tag.edit.processAsFlow" --include=*.kt .` returns nothing; `./gradlew :core:component:tag:testDebugUnitTest :feature:home:testDebugUnitTest`
6. Confirm the Hilt graph and coverage: assemble the app and check Jacoco for `core:component:tag` (no drop for the moved logic; add `@ExcludeFromGeneratedTestReport` to the class only if it appears as uncovered).
   - **Verify**: `./gradlew :app:assembleDebug` and `./gradlew :core:component:tag:jacocoTestReport`

## Verification
testStrategy: unit

- No Compose or layout UI is touched (changes are limited to the tag component's logic/DI, `HomeEnvironment`, `HomeReduce`, and tests), so Maestro UI QA is not needed.
- Unit tests scoped to changed modules: `./gradlew :core:component:tag:testDebugUnitTest :feature:home:testDebugUnitTest`.
- Hilt wiring is compile-time checked by `./gradlew :app:assembleDebug`.
- Acceptance criteria mapping: no top-level extension consumed (Task 5 grep), `HomeReduce` receives it via `HomeEnvironment` (Task 3), no static mocking in `HomeReduceKtTest` (Task 4 grep), `processAsFlow` scenarios still covered and passing (Task 2), all affected module tests pass (Task 5/6).

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-09 | Initial draft | requirements.md, user decisions on Open Questions |
| 2026-11-09 | Plain interface instead of `fun interface`, mockk-based test approach; renamed `TagEditTaskProcessor` to `ProcessTagEditTaskAsFlowUseCase` (no `-er` names, follows `...UseCase` convention) | user change request |
| 2026-11-09 | Dropped interface, `Impl`, and `@Binds` module; single concrete `@Reusable @Inject` UseCase class named per the Android domain-layer convention; tests mock the concrete class | user change request |
| 2026-11-09 | Final name `GetTagEditTaskTransitionFlowUseCase` (field `getTagEditTaskTransitionFlow`, test `GetTagEditTaskTransitionFlowUseCaseTest`); Naming rationale rewritten (cold flow, not an execution verb). Earlier rows keep the previous names as history. | user decision |
| 2026-11-10 | `HomeReduce` call sites changed to chaining via `.let(environment.getTagEditTaskTransitionFlow::invoke)` (the reviewer's `environment::getTagEditTaskTransitionFlow` does not compile: it is a property reference, not `invoke`); test helpers renamed `mock*` to `gen*` | PR #914 review |
| 2026-11-10 | Test helpers renamed to `genGetTagEditTaskTransitionFlowUseCase` (the `gen` convention uses the return type name). Moving them to `tag-test` was tried and reverted: single consumer, and `tag-test` holds faker-based generators only, no mockk | PR #914 review, user decision |
