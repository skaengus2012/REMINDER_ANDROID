/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * @author Doohyun
 */
internal fun Project.configureJacocoToolVersion() {
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }
}

internal fun Project.registerJacocoTestReportTask(
    name: String,
    testTaskName: String,
    configuration: JacocoReport.() -> Unit
): TaskProvider<JacocoReport> = tasks.register<JacocoReport>(name) {
    group = "verification"
    reports {
        xml.required = true
        html.required = true
    }
    dependsOn(testTaskName)
    executionData.setFrom(file("${layout.buildDirectory.get()}/jacoco/$testTaskName.exec"))
    configuration()
}

internal val jacocoExcludePatterns = setOf("**/infra/**")

internal const val jacocoTestReportTaskDefaultName = "jacocoTestReport"