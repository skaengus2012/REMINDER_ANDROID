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

import com.nlab.reminder.configureStdFeatureDependencies
import com.nlab.reminder.libs
import org.gradle.kotlin.dsl.apply

apply(plugin = "com.android.library")
apply(plugin = "nlab.android.library.component")
apply(plugin = "nlab.android.library.di")
apply(plugin = "nlab.android.library.jacoco")
apply(plugin = "nlab.android.library.statekit")

configureStdFeatureDependencies()

dependencies {
    "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
    "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
}