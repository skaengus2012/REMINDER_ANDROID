# Commit Rules

## Format

```
<Type> #<issue-number> - <subject>
```

- **Type**: `Implement` / `Fix` / `Refactor` / `Chore`
- **Issue number**: GitHub issue number, or `N/A` if not applicable
- **Subject**: Written in English

---

## Types

| Type | When to use |
|------|-------------|
| `Implement` | New feature or functionality |
| `Fix` | Bug fix |
| `Refactor` | Code restructuring without behavior change |
| `Chore` | Build config, dependencies, and other miscellaneous tasks |

---

## Body (Implement only)

`Implement` commits require a detailed body describing **what** was changed and **why** it was implemented that way.

```
Implement #<issue-number> - <subject>

- <reason or detail>
- <reason or detail>
- <reason or detail>
```

---

## AI Collaboration (Co-authored-by)

When working with an AI coding assistant (like Antigravity) and you want to attribute the work to both yourself and the AI, include the `Co-authored-by` trailer at the bottom of the commit message (separated by an empty line).

```
Implement #789 - Add agent guidelines structure and commit rules

- Created docs/guidelines/ directory to house project-wide guidelines
- Added commit-rules.md defining the commit message format

Co-authored-by: AI-Assistant <assistant@example.com>
```

---

## Examples


### Implement
```
Implement #312 - Add tag filtering to ScheduleListScreen

- Introduced TagFilterBar composable above the schedule list to allow users to filter by tag
- Filter state is hoisted to ScheduleListViewModel and exposed via UiState to follow MVI conventions
- Filtering is applied in the use case layer to keep the ViewModel free of business logic
```

### Fix
```
Fix #401 - Resolve StateFlow not emitting on configuration change
```

### Refactor
```
Refactor #278 - Extract alarm scheduling logic into AlarmScheduler use case
```

### Chore
```
Chore N/A - Upgrade Kotlin to 2.0.0 and update related dependencies
```
