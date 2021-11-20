/*
 * Copyright (C) 2018 The N's lab Open Source Project
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
 *
 */

object Dependencies {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib:${DependenciesVersions.KOTLIN}"
    const val KOTLIN_COROUTINE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependenciesVersions.KOTLIN_COROUTINE}"
    const val KOTLIN_COROUTINE_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${DependenciesVersions.KOTLIN_COROUTINE}"

    const val ANDROID_APPCOMPAT = "androidx.appcompat:appcompat:${DependenciesVersions.ANDROID_APPCOMPAT}"
    const val ANDROID_KTX = "androidx.core:core-ktx:${DependenciesVersions.ANDROID_KTX}"
    const val ANDROID_MATERIAL = "com.google.android.material:material:${DependenciesVersions.ANDROID_MATERIAL}"
    const val ANDROID_CONSTRAINTLAYOUT = "androidx.constraintlayout:constraintlayout:${DependenciesVersions.ANDROID_CONSTRAINTLAYOUT}"
    const val ANDROID_RECYCLERVIEW = "androidx.recyclerview:recyclerview:${DependenciesVersions.ANDROID_RECYCLERVIEW}"
    const val ANDROID_LIFECYCLE_VIEWMODEL_KTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${DependenciesVersions.ANDROID_LIFECYCLE}"
    const val ANDROID_LIFECYCLE_RUNTIME_KTX = "androidx.lifecycle:lifecycle-runtime-ktx:${DependenciesVersions.ANDROID_LIFECYCLE}"

    const val ANDROID_COMPOSE_UI = "androidx.compose.ui:ui:${DependenciesVersions.ANDROID_COMPOSE}"
    const val ANDROID_COMPOSE_UI_TOOLING = "androidx.compose.ui:ui-tooling:${DependenciesVersions.ANDROID_COMPOSE}"
    const val ANDROID_COMPOSE_FOUNDATION = "androidx.compose.foundation:foundation:${DependenciesVersions.ANDROID_COMPOSE}"
    const val ANDROID_COMPOSE_MATERIAL = "androidx.compose.material:material:${DependenciesVersions.ANDROID_COMPOSE}"

    const val ANDROID_ACTIVITY_COMPOSE = "androidx.activity:activity-compose:${DependenciesVersions.ANDROID_ACTIVITY_COMPOSE}"

    const val TEST_JUNIT = "junit:junit:${DependenciesVersions.TEST_JUNIT}"
    const val TEST_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${DependenciesVersions.TEST_COROUTINES}"
    const val TEST_MOCKITO = "org.mockito:mockito-inline:${DependenciesVersions.TEST_MOCKITO}"
    const val TEST_MOCKITO_KOTLIN = "org.mockito.kotlin:mockito-kotlin:${DependenciesVersions.TEST_MOCKITO_KOTLIN}"
    const val TEST_ANDROID_JUNIT_EXT = "androidx.test.ext:junit:${DependenciesVersions.TEST_ANDROID_JUNIT_EXT}"
    const val TEST_ANDROID_JUNIT_ESPRESSO = "androidx.test.espresso:espresso-core:${DependenciesVersions.TEST_ANDROID_ESPRESSO}"
    const val TEST_ANDROID_TEST_RUNNER = "androidx.test:runner:${DependenciesVersions.TEST_ANDROID_RUNNER}"
    const val TEST_ANDROID_TEST_RULES = "androidx.test:rules:${DependenciesVersions.TEST_ANDROID_RULES}"
}
