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

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.SourceDirectories
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * @author Doohyun
 */
internal fun Project.configureJacocoAndroid(
    commonExtension: CommonExtension,
    androidComponentExtension: AndroidComponentsExtension<*, *, *>
) {
    commonExtension.buildTypes.forEach { it.enableUnitTestCoverage = true }
    commonExtension.buildTypes.named("debug") {
        // Configure only the debug build, otherwise it will force the debuggable flag on release buildTypes as well
        enableAndroidTestCoverage = true
    }

    configureJacocoToolVersion()
    val jacocoReportTaskStub = tasks.register(jacocoTestReportTaskDefaultName) {
        group = "verification"
    }

    androidComponentExtension.onVariants { variant ->
        val variantName = variant.name.capitalized()
        val testTaskName = "test${variantName}UnitTest"

        val myObjFactory = project.objects
        val allJars: ListProperty<RegularFile> = myObjFactory.listProperty(RegularFile::class.java)
        val allDirectories: ListProperty<Directory> = myObjFactory.listProperty(Directory::class.java)

        val reportTask = registerJacocoTestReportTask(name = "jacocoTestReport${variantName}") {
            dependsOn(testTaskName)
            executionData.setFrom(
                fileTree(layout.buildDirectory.dir("jacoco")) {
                    include("$testTaskName.exec")
                },
                fileTree(layout.buildDirectory.dir("outputs/unit_test_code_coverage")) {
                    include("**/$testTaskName.exec")
                }
            )

            fun SourceDirectories.Flat?.toFilePaths(): Provider<List<String>> = this
                ?.all
                ?.map { directories -> directories.map { it.asFile.path } }
                ?: provider { emptyList() }
            sourceDirectories.setFrom(
                files(
                    variant.sources.java.toFilePaths(),
                    variant.sources.kotlin.toFilePaths(),
                ),
            )
            classDirectories.setFrom(
                allJars,
                allDirectories.map { dirs ->
                    dirs.map { dir ->
                        myObjFactory.fileTree().setDir(dir).exclude(coverageExclusions)
                    }
                },
            )
        }
        jacocoReportTaskStub.configure {
            finalizedBy(reportTask)
        }
        variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
            .use(reportTask)
            .toGet(
                ScopedArtifact.CLASSES,
                { _ -> allJars },
                { _ -> allDirectories },
            )
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

private val coverageExclusions = jacocoExcludePatterns + setOf(
    "**/R.class",
    "**/R$*.class",
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/android/**",
    "**/androidx/**/compose/**",
    "**/compose/**",
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