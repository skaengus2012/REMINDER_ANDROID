# Requirements Analysis: [Refactor] Make TagEditTask.processAsFlow an explicit dependency instead of a top-level extension function (#910)

- Issue: https://github.com/skaengus2012/REMINDER_ANDROID/issues/910
- Parent issue: None
- Labels: Refactor
- Created: 2026-11-08
- Last Updated: 2026-11-09
- Analysis Baseline: 2026-11-09T13:07:38Z

## Background and Problem
`TagEditTask.processAsFlow` is a top-level extension function. It works well, but callers depend on it implicitly. As a result, unit tests of its callers (e.g. `HomeReduceKtTest`) have to mock it with global static mocking (`mockkStatic(TagEditTask::processAsFlow)`), which hides the dependency and makes the tests fragile.

## Goals
Make `processAsFlow` an explicit, injectable dependency of its callers so that caller tests no longer need static mocking, without changing behavior.

## Scope
### In Scope
- Refactoring `TagEditTask.processAsFlow` into an injectable form (e.g. a function type or an interface).
- Updating callers (e.g. `HomeReduce`) to receive the dependency explicitly.
- Replacing static mocking in caller tests (e.g. `HomeReduceKtTest`) with plain fakes/mocks of the explicit dependency.

### Out of Scope
- Changing the behavior of `processAsFlow`.
- Changing the existing test coverage of `processAsFlow`.

## Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| FR-1 | `TagEditTask.processAsFlow` can be injected as an explicit dependency (e.g. function type or interface) instead of being a top-level extension function. | Body |
| FR-2 | Callers (e.g. `HomeReduce`) receive the dependency explicitly. | Body |
| FR-3 | Caller tests (e.g. `HomeReduceKtTest`) use plain fakes/mocks of the explicit dependency instead of `mockkStatic(TagEditTask::processAsFlow)`. | Body |

## Non-Functional Requirements
| ID | Requirement | Source |
|----|-------------|--------|
| NFR-1 | The existing behavior of `processAsFlow` is kept unchanged. | Body |
| NFR-2 | The existing test coverage of `processAsFlow` is kept unchanged. | Body |

## Acceptance Criteria
- [ ] `processAsFlow` is no longer consumed as a top-level extension function by callers; it is provided as an explicit dependency.
- [ ] `HomeReduce` receives the dependency explicitly.
- [ ] `HomeReduceKtTest` contains no `mockkStatic(TagEditTask::processAsFlow)` / `unmockkStatic`.
- [ ] `TagEditTaskKtTest` still covers `processAsFlow` as before and passes.
- [ ] All existing tests of affected modules pass.

## Impact
Confirmed from the code:
- `core/component/tag/.../edit/TagEditTask.kt`: defines `fun TagEditTask.processAsFlow()`.
- `core/component/tag/.../edit/TagEditTaskKtTest.kt`: calls `task.processAsFlow()` (4 places).
- `feature/home/.../HomeReduce.kt`: `HomeReduce(environment: HomeEnvironment)` calls `.processAsFlow()` at 5 places and imports it directly.
- `feature/home/.../HomeReduceKtTest.kt`: stubs it via `every { processAsFlow() }` and `mockkStatic`/`unmockkStatic`.
- `HomeEnvironment` and the Hilt wiring may be affected if the dependency is provided through the environment (not yet examined).

## Open Questions
- Form of the dependency: function type (e.g. typealias) or interface? The issue only gives both as examples.
- Where should the dependency be held: a new field of `HomeEnvironment`, an additional parameter of `HomeReduce(...)`, or elsewhere?
- Should the `TagEditTask.processAsFlow` extension remain (e.g. as the real implementation), or be removed entirely? If kept, should it stay public?
- Are there callers besides `HomeReduce` that should be covered ("e.g." in the issue hints at more)? Only `HomeReduce` was found in the current code.
- How should the real implementation be provided via Hilt (module location, scope)?
- Should `TagEditTaskKtTest` be kept as-is (testing the real implementation), or restructured depending on the chosen form?

## Conflicts and Notes
There are no comments and no parent issue, so there are no conflicts between sources.

---

## Change History
| Date | Change | Source |
|------|--------|--------|
| 2026-11-09 | Initial draft | Body |
