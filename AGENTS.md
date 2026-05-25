# Reminder Android Project (AI Agent Guide)

This file serves as a guide for AI coding assistants (agents) to understand the architecture, module layout, and build/test workflows of the **REMINDER_ANDROID** project.

---

## 🏗️ Architecture & Tech Stack

This project is a modular, reactive Android application that focuses on clean architecture, loose coupling, and testability.

*   **State Management / Architecture:** Strictly follows the **MVI (Model-View-Intent)** architecture pattern.
    *   State flows are managed using **StateKit** (`:statekit` modules), which is a custom lightweight MVI/state management framework leveraging Kotlin Coroutines and Flows.
*   **Dependency Injection (DI):** Managed via **Hilt**. Convention plugins (e.g., `nlab.android.hilt`, `nlab.android.library.di`) are defined under `build-logic` to enforce consistent DI setups.
*   **UI:** Built primarily with **Jetpack Compose** (via `nlab.android.library.compose` convention) alongside traditional view architecture components where necessary.
*   **Asynchronous Processing:** Powered by **Kotlin Coroutines** and **Flows**.
*   **Testing Strategy:** Heavily practices **TDD (Test-Driven Development)**, aiming for near 100% test coverage. Jacoco plugins (`nlab.android.library.jacoco`, `nlab.android.application.jacoco`) are integrated for code coverage reporting.

---

## 📦 Module Structure

The project is highly modularized to ensure separation of concerns and faster build times:

```
REMINDER_ANDROID/
├── app/                      # Main application entry point & Hilt setup
├── feature/                  # Feature-level modules
│   ├── home/                 # Main home screen feature
│   └── all/                  # Aggregate or common feature entry points
├── core/                     # Shared foundation and utility modules
│   ├── component/            # Reusable UI components (e.g., schedulelist, tag, toolbar, usermessage)
│   ├── data/                 # Data layer definitions
│   ├── data-impl/            # Repository and data access implementations
│   ├── local/                # Local database / Room persistence
│   ├── network/              # Remote network API access (Retrofit, etc.)
│   ├── designsystem/         # Design system tokens, themes, and basic UI elements
│   ├── android/              # Platform wrappers and context utilities
│   └── androidx/             # Compose, Fragment, Recyclerview, and Navigation wrappers
├── statekit/                 # Custom state management framework (core, dsl, lifecycle, foundation)
└── testkit/                  # Shared test utilities and helper modules
```

*   **Helper Test Modules:** Core component modules have matching `-test` modules (e.g., `:core:component:tag-test`, `:core:component:schedulelist-test`) containing mock data, test configurations, and utilities to isolate test scopes.

---

## 🛠️ Build & Test Commands

Use the following Gradle commands to compile, check code formatting, and run tests.

### Build and Compile
*   **Assemble Debug APK:**
    ```bash
    ./gradlew :app:assembleDebug
    ```
*   **Clean Build:**
    ```bash
    ./gradlew clean build
    ```

### Running Tests (TDD)
This project requires high code coverage. Always verify your changes with tests before submitting code reviews.

*   **Run All Unit Tests:**
    ```bash
    ./gradlew test
    ```
*   **Run Tests for a Specific Module:**
    ```bash
    ./gradlew :core:component:schedulelist:test
    ```
*   **Run Instrumented/Android Tests:**
    ```bash
    ./gradlew connectedAndroidTest
    ```

### Code Coverage (Jacoco)
*   **Generate Jacoco Test Reports:**
    ```bash
    ./gradlew jacocoTestReport
    ```
