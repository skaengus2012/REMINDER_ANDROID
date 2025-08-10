@file:Suppress("UnstableApiUsage")

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

// Prevent 'Unable to make progress running work'
// https://issuetracker.google.com/issues/328871352
gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:convention:testClasses"))

// nlab-kits.
include(
    ":statekit:core",
    ":statekit:dsl",
    ":statekit:test",
    ":testkit"
)
// Planeat
include(
    ":app"
)
include(
    ":core:android",
    ":core:androidx:compose",
    ":core:androidx:fragment",
    ":core:androidx:lifecycle",
    ":core:androidx:navigation-compose",
    ":core:androidx:recyclerview",
    ":core:androidx:transition",
    ":core:annotation",
    ":core:component:currenttime",
    ":core:component:displayformat",
    ":core:component:tag",
    ":core:component:tag-test",
    ":core:component:usermessage",
    ":core:component:usermessage-eventbus",
    ":core:component:usermessage-test",
    ":core:data",
    ":core:data-di",
    ":core:data-ext",
    ":core:data-impl",
    ":core:data-platform-impl",
    ":core:data-test",
    ":core:designsystem",
    ":core:inject",
    ":core:kotlin",
    ":core:kotlin-test",
    ":core:kotlinx:coroutines",
    ":core:kotlinx:coroutines-android",
    ":core:kotlinx:datetime",
    ":core:local",
    ":core:network",
    ":core:schedule",
    ":core:schedule-ext",
    ":core:schedule-test",
    ":core:statekit",
    ":core:translation",
    ":core:uitext",
    ":core:uitext-test",
)
include(
    ":feature:home"
)