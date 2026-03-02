/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * @author Doohyun
 */
internal fun Project.configureComposeAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        buildFeatures.compose = true
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        "implementation"(platform(bom))
        "androidTestImplementation"(platform(bom))

        "implementation"(libs.findLibrary("androidx-compose-runtime").get())
        "implementation"(libs.findLibrary("androidx-compose-ui").get())
        "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        configureComposeCompilerReport(extension = this)
        stabilityConfigurationFiles.set(
            listOf(
                rootProject
                    .layout
                    .projectDirectory
                    .file("compose_compiler_stability.conf")
            )
        )
    }
}

/**
 * Generate reports according to enableComposeCompilerMetrics and enableComposeCompilerReports settings.
 *
 * Example:
 * ```
 * ./gradlew assembleRelease -PenableComposeCompilerMetrics=true -PenableComposeCompilerReports=true
 * ```
 */
private fun Project.configureComposeCompilerReport(
    extension: ComposeCompilerGradlePluginExtension,
) {
    fun Provider<String>.onlyIfTrue() =
        flatMap { provider { it.takeIf(String::toBoolean) } }
    fun Provider<*>.relativeToRootProject(dir: String) =
        flatMap { rootProject.layout.buildDirectory.dir(projectDir.toRelativeString(rootDir)) }.map { it.dir(dir) }
    project.providers.gradleProperty("enableComposeCompilerMetrics")
        .onlyIfTrue()
        .relativeToRootProject("compose-metrics")
        .let(extension.metricsDestination::set)
    project.providers.gradleProperty("enableComposeCompilerReports")
        .onlyIfTrue()
        .relativeToRootProject("compose-reports")
        .let(extension.reportsDestination::set)
}