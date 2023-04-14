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

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

/**
 * @author Doohyun
 */
internal fun Project.configureJacocoToolVersion() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }
}

val BuildType.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)

val ProductFlavor.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)