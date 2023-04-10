/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.provideDelegate

/**
 * @author Doohyun
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = 33

        defaultConfig {
            minSdk = 23
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        java {
            // fix warning [https://github.com/skaengus2012/REMINDER_ANDROID/issues/82#issuecomment-1406942682]
            // see https://kotlinlang.org/docs/gradle-configure-project.html#check-for-jvm-target-compatibility-of-related-compile-tasks
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_11.toString()))
            }
        }

        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()

            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()

            // Exclude opt-in API warnings
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
            )
        }

        packagingOptions {
            // guide in kotlin coroutine
            // https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            resources.excludes.add("DebugProbesKt.bin")
        }
    }
}