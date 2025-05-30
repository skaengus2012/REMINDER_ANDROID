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
import com.nlab.reminder.kspExtension
import com.nlab.reminder.libs
import org.gradle.kotlin.dsl.dependencies

apply(plugin = "com.google.devtools.ksp")
apply(plugin = "dagger.hilt.android.plugin")

dependencies {
    "implementation"(libs.findLibrary("google.hilt.android").get())
    "ksp"(libs.findLibrary("google.hilt.android.compiler").get())
}

// guide in dagger hilt
// https://developer.android.com/training/dependency-injection/hilt-android?hl=ko#setup
kspExtension.arg("correctErrorTypes", "true")