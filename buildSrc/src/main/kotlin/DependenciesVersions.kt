import org.gradle.api.JavaVersion

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

/**
 * @author Doohyun
 */
object DependenciesVersions {
    val JAVA_VERSION = JavaVersion.VERSION_11
    const val AGP = "7.3.1"
    const val JACOCO = "0.8.8"
    const val KOTLIN = "1.7.21"
    const val KOTLIN_COROUTINE = "1.6.4"

    const val ANDROID_APPCOMPAT = "1.5.1"
    const val ANDROID_CORE = "1.9.0"
    const val ANDROID_MATERIAL = "1.6.1"
    const val ANDROID_CONSTRAINTLAYOUT = "2.1.4"
    const val ANDROID_RECYCLERVIEW = "1.2.1"
    const val ANDROID_LIFECYCLE = "2.5.1"
    const val ANDROID_FRAGMENT = "1.5.4"
    const val ANDROID_NAVIGATION = "2.5.3"
    const val ANDROID_ROOM = "2.4.3"
    const val ANDROID_STARTUP = "1.1.1"
    const val ANDROID_DATASTORE_PREFERENCES = "1.0.0"

    const val GOOGLE_HILT = "2.44"
    const val GOOGLE_FLEXBOX = "3.0.0"

    const val GLIDE = "4.14.2"

    const val JSOUP = "1.13.1"

    const val FACEBOOK_FLIPPER = "0.173.0"
    const val FACEBOOK_SOLOADER = "0.10.4"

    const val SQUARE_LEAKCANARY = "2.9.1"

    const val TEST_JUNIT = "4.13.2"
    const val TEST_COROUTINES = "1.6.4"
    const val TEST_MOCKITO = "4.8.1"
    const val TEST_MOCKITO_KOTLIN = "4.0.0"
    const val TEST_JAVAFAKER = "1.0.2"
    const val TEST_ANDROID_JUNIT_EXT = "1.1.3"
    const val TEST_ANDROID_ESPRESSO = "3.3.0"
    const val TEST_ANDROID_RUNNER = "1.1.0"
    const val TEST_ANDROID_RULES = "1.1.0"
}