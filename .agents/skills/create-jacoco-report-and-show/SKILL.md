---
name: create-jacoco-report-and-show
description: >-
  Generates a Jacoco test coverage report for a specific Gradle module after running clean to prevent caching issues, and opens the resulting HTML report in the browser.
---

# Jacoco Test Report Generator & Viewer Skill

## Overview
This skill generates the Jacoco test coverage report for a specific Android/Kotlin module and opens it in the browser. To prevent the issue where results from a previously built module are incorrectly displayed due to caching, it runs the `clean` task on the target module before generating the report.

## Instructions for the Agent

When invoked, the agent must proceed with the following steps:

### 1. Intelligent Module Recommendation and Prompt (Git-based)
If the user does not specify a module name (e.g., `:core:component:tag`) in their prompt, deduce and recommend the target module using the following priorities:

- **Priority 1 (Uncommitted Changes)**:
  - Use the `run_command` tool to execute `git diff --numstat` to check for uncommitted changes.
  - Analyze the file paths with the highest number of added/deleted code lines (top 1~3) to infer the Gradle modules they belong to (e.g., `:core:component:tag`).
- **Priority 2 (Recent Commits)**:
  - If there are no uncommitted changes, execute `git log -n 5 --numstat` to infer the top 1~3 most modified modules from recent commits.

Present the deduced top 1~3 modules as multiple-choice options using the `ask_question` tool.
Since the user's desired module might not be in the candidates, you must always include an **`Other (Manual Input)`** option.

### 2. Dynamic Task Discovery
Because the exact name of the Jacoco task can vary depending on the module type (e.g., `jacocoTestReportJvm`, `jacocoTestReportDebug`, `jacocoTestReportRelease`, `jacocoTestReport`), you must dynamically query the available tasks for the selected module.
- Use the `run_command` tool to execute the following command:
  ```bash
  ./gradlew <module>:tasks --all | grep jacocoTestReport
  ```
- Parse the output to extract all available Jacoco tasks for that module.
- Present the extracted tasks to the user as multiple-choice options using the `ask_question` tool so they can select the exact task they want to execute.

### 3. Execute Gradle Command (Cache Reset & Report Generation)
- Once the specific task is selected, use the `run_command` tool to execute the following command:
  ```bash
  ./gradlew <module>:clean <module>:<selected_task>
  ```
  *(Note: Replace `<module>` and `<selected_task>` with the user's choices.)*

### 4. Locate the Report File
- After generation is complete, find the `index.html` file within the module's directory path.
- Execute the `find` command based on the module path (replace colons `:` with slashes `/`):
  ```bash
  find <module-path>/build -name index.html | grep jacoco
  ```
  *(Example: For module `:statekit:core`, search within `statekit/core/build`)*

### 5. Open Browser and Provide Result
- Use the **absolute path** of the found `index.html` file to execute the Mac browser open command:
  ```bash
  open <absolute_path_to_index.html>
  ```
- Provide a markdown link using the `file://` scheme in the chat so the user can easily click it:
  ```markdown
  [Jacoco Coverage Report](file://<absolute_path_to_index.html>)
  ```
