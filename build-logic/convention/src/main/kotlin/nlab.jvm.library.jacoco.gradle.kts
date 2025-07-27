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

import com.nlab.reminder.configureJacocoToolVersion
import com.nlab.reminder.jacocoExcludePatterns
import com.nlab.reminder.jacocoTestReportTaskDefaultName
import com.nlab.reminder.registerJacocoTestReportTask
import org.gradle.api.plugins.JavaPlugin

apply(plugin = "jacoco")
apply(plugin = "org.jetbrains.kotlin.jvm")

configureJacocoToolVersion()

val jacocoTestReportJvmTask = registerJacocoTestReportTask(
    name = "jacocoTestReportJvm",
    testTaskName = JavaPlugin.TEST_TASK_NAME
) {
    classDirectories.setFrom(files(classDirectories.map { dir ->
        fileTree(dir) { exclude(jacocoExcludePatterns) }
    }))
}

tasks.named(jacocoTestReportTaskDefaultName) {
    finalizedBy(jacocoTestReportJvmTask)
}