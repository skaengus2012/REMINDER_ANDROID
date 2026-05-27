---
name: git-worktree-creator
description: >-
  Analyzes git repository context (branches, commit history) to recommend prefixes,
  worktree names (issue-based), and base branches, and safely creates a git worktree.
---

# Git Worktree Creator Skill

## Overview

This skill recommends and safely creates a Git worktree by analyzing the local Git repository context.
To avoid security warnings and permission requests in sandboxed environments, **this skill does not use external Python scripts. Instead, the agent runs standard Git commands and uses the `ask_question` tool to interactively walk the user through the configuration steps.**

Worktrees are always created under the **`.agents/worktrees/` directory** inside the repository root.

---

## 🛠️ Step 1. Git Context Analysis

The agent must first run the following commands to inspect the repository context:

```bash
# 1. List local branches
git branch --format="%(refname:short)"

# 2. Get the last 10 commit messages
git log -n 10 --oneline

# 3. Get the last 30 reflog entries
git reflog -n 30
```

---

## 🛠️ Step 2. Recommend & Ask User (Interactive Mode)

Based on the Git command output, the agent collects recommendation options:
- **Prefixes**: Typically `feature`, or no prefix.
- **Names (Issue-based)**: Scan commit logs and reflogs for issue numbers (`issue123`, `issue-123`, `#123`). Recommend name choices like `issue123` and `worktree-temp`.
- **Base Branches**: `master` or `main` (if present), the current active branch, and other local branches.

### The Interactive Prompts:
The agent **MUST** call the `ask_question` tool with the compiled options. Do not make assumptions. Provide a multi-choice questionnaire:
1. **Question 1**: Choose a branch prefix (e.g., `feature`, `(No Prefix)`).
2. **Question 2**: Choose a worktree name (list the detected issue names, plus a custom write-in fallback).
3. **Question 3**: Choose a base branch (list `master`, `main`, the current active branch, etc.).

---

## 🛠️ Step 3. Worktree Creation Process

Once the user selects the configuration:
* **Branch name**: `<prefix>/<name>` (or just `<name>` if prefix was omitted).
* **Base branch**: The selected base branch.
* **Target path**: `.agents/worktrees/<safe_branch_name>` (where `/` in the branch name is replaced with `_`).

Perform the following steps:

### 1. Check Directory Existence
Verify that the target directory does not exist yet to prevent conflicts.

### 2. Run the Git Worktree Command
Check if the selected branch already exists in the repository:
* **If branch already exists**: (Ignore the base branch selection)
  ```bash
  git worktree add .agents/worktrees/<safe_branch_name> <branch_name>
  ```
* **If branch does not exist**:
  ```bash
  git worktree add .agents/worktrees/<safe_branch_name> -b <branch_name> <base_branch>
  ```

---

## ⚠️ Common Mistakes & Troubleshooting

1. **Parent Directory Missing**: If `.agents/worktrees` does not exist, run `mkdir -p .agents/worktrees` before adding the worktree.
2. **Branch Already Checked Out**: Git prevents checking out the same branch in multiple active worktrees. If this happens, ask the user to provide a different name.
