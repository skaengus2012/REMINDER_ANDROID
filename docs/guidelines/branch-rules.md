# Branch Rules

This document defines the branching strategy and naming conventions for the REMINDER_ANDROID project. Agents and developers must read this document before starting any tasks to select the correct target branch.

---

## 1. Allowed Branch Types

Developers and agents create only the following three types of branches (see [Tool-generated Branches](#tool-generated-branches) for the exceptions):

* **`master`**
  * The main production-ready branch. It must always be buildable and stable.
* **`epic/issue<issue-number>`**
  * Inspired by Jira's Epic. Used for large-scale tasks or major features that contain multiple sub-tasks.
  * Example: `epic/issue795`
* **`feature/*`**
  * Represents a single task or a work branch.
  * **Important**: Regardless of the task nature (e.g., Fix, Refactor, Chore), all of these branches must use the `feature/` prefix.
  * Default format: `feature/issue<issue-number>`. Use it for every task that has an issue.
    * Example: `feature/issue796`
  * Merge work branches (e.g., resolving conflicts between two branches): `feature/<source>-into-<target>-<timestamp>`. Do not use a `merge/` prefix.
    * Example: `feature/master-into-epic-issue601-20261125T125543Z`
  * Any other name under `feature/` is also allowed.

### Tool-generated Branches

The following branches are created by tools and are also allowed:

* **`revert-<pr-number>-<original-branch>`**: created by the GitHub Revert button.
* **`dependabot/...`**: created by Dependabot.

---

## 2. Branch Merging Strategy & Workflow

The branching and merging flow depends on the scale of the task.

### Scenario A: Large-scale Work (Epic-based Development)
For larger work composed of multiple sub-tasks:

1. **Create Epic Branch**: Create an `epic/issue<issue-number>` branch from `master`.
2. **Create Feature Branch**: Create a `feature/issue<issue-number>` branch from the `epic/issue<issue-number>` branch.
3. **Merge to Epic**: Once the feature is complete, **create a Pull Request targeting the `epic/issue<issue-number>` branch** (not `master`) and merge it.
4. **Final Merge to Master**: Once all sub-feature branches are merged and the epic is complete, merge the `epic/issue<issue-number>` branch into `master`.

```mermaid
gitGraph
    commit id: "Initial"
    branch epic/issue100
    checkout epic/issue100
    commit id: "Epic Start"
    branch feature/issue101
    checkout feature/issue101
    commit id: "Task 1"
    checkout epic/issue100
    merge feature/issue101
    branch feature/issue102
    checkout feature/issue102
    commit id: "Task 2"
    checkout epic/issue100
    merge feature/issue102
    checkout master
    merge epic/issue100 id: "Merge Epic to Master"
```

### Scenario B: Small-scale Work (Standalone Task Development)
For simple bug fixes, refactoring, or small standalone features:

1. **Create Feature Branch**: Create a `feature/issue<issue-number>` branch from `master`.
2. **Merge to Master**: Once complete, **create a Pull Request targeting `master`** and merge it directly.

```mermaid
gitGraph
    commit id: "Initial"
    branch feature/issue201
    checkout feature/issue201
    commit id: "Fix / Implement"
    checkout master
    merge feature/issue201 id: "Merge Task to Master"
```

---

## 3. Enforcement

The GitHub branch ruleset `Branch naming (see docs/guidelines/branch-rules.md)` blocks creating a remote branch whose name is not allowed. It does not check local branches, and it does not restrict pushes to branches that already exist.

| Setting | Value |
|---------|-------|
| Enforcement | Active |
| Bypass list | Empty |
| Target | All branches, excluding the patterns below |
| Rules | `Restrict creations` only |

Excluded (allowed) patterns:

```text
master
epic/issue[0-9]*
feature/**/*
revert-*
revert-*/**/*
dependabot/**/*
```

The patterns are looser than the naming rules above (e.g., `epic/issue[0-9]*` also matches `epic/issue1abc`), so follow the rules even when the ruleset accepts a name.

To check whether a name is restricted before pushing:

```bash
gh ruleset check <branch-name>
```

**Important**: The ruleset is a repository setting and is not synced with this document. Whenever the naming rules change, update the ruleset as well.

