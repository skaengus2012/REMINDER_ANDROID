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

package com.nlab.reminder

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * @author Doohyun
 */
internal fun Project.configureJacocoAndroid(extension: AndroidComponentsExtension<*, *, *>) {
    configureJacocoToolVersion()
    val jacocoReportTaskStub = tasks.register(jacocoTestReportTaskDefaultName) {
        group = "verification"
    }
    extension.onVariants { variant ->
        val jacocoTestReportForVariant = registerJacocoTestReportTask(
            name = "jacocoTestReport${variant.name.capitalized()}",
            testTaskName = "test${variant.name.capitalized()}UnitTest",
        ) {
            sourceDirectories.setFrom(androidJacocoSourcesDirectories(variant))
            classDirectories.setFrom(androidJacocoClassDirectories(variant))
        }
        jacocoReportTaskStub.configure {
            finalizedBy(jacocoTestReportForVariant)
        }

        tasks.withType<Test>().configureEach {
            configure<JacocoTaskExtension> {
                // Required for JaCoCo + Robolectric
                // https://github.com/robolectric/robolectric/issues/2230
                isIncludeNoLocationClasses = false

                // Required for JDK 11 with the above
                // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

private fun Project.androidJacocoSourcesDirectories(variant: Variant): ConfigurableFileCollection =
    files(
        "$projectDir/src/main/java",
        "$projectDir/src/main/kotlin",
        "$projectDir/src/${variant.name}/java",
        "$projectDir/src/${variant.name}/kotlin"
    )

private fun Project.androidJacocoClassDirectories(variant: Variant): ConfigurableFileTree =
    fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/${variant.name}") {
        exclude(
            jacocoExcludePatterns + setOf(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/android/**",
                "**/di/**",
                "**/fake/**",
                "**/navigation/**",
                "**/startup/**",
                "**/test/**",
                "**/ui/**",

                /* filtering unnecessary feature components */
                "**/*Action$*.class",
                "**/*UiState$*.class",
                "**/*Environment.class",

                /* filtering Navigation Component generated classes */
                "**/*Args*.*",
                "**/*Directions*.*",
            )
        )
    }