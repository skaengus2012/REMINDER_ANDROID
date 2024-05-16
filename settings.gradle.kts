/*
 * Copyright (C) 2022 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
pluginManagement {
    repositories {
        includeBuild("build-logic")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "REMINDER_ANDROID"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// nlab-kits.
include(
    ":statekit:compiler",
    ":statekit:core",
    ":statekit:runtime",
    ":statekit:test",
    ":statekit:viewmodel",
    ":testkit"
)
// Reminder
include(
    ":app"
)
include(
    ":core:android",
    ":core:androidx:fragment",
    ":core:androidx:fragment-compose",
    ":core:androidx:lifecycle",
    ":core:androidx:recyclerview",
    ":core:androidx:transition",
    ":core:annotation",
    ":core:annotation-android",
    ":core:data",
    ":core:data-di",
    ":core:data-ext",
    ":core:data-impl",
    ":core:data-test",
    ":core:designsystem:compose",
    ":core:di",
    ":core:domain",
    ":core:domain-di",
    ":core:kotlin",
    ":core:kotlinx-coroutine",
    ":core:local",
    ":core:local-di",
    ":core:schedule",
    ":core:schedule-ext",
    ":core:schedule-test",
    ":core:ui:compose",
    ":core:ui:view"
)