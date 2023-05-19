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

// FIXME Annotations must be added before Gradle 8.1.
// FIXME https://developer.android.com/studio/build/migrate-to-catalogs?hl=ko#migrate-plugins
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.navigation.safearges) apply false
    alias(libs.plugins.google.hilt) apply false
}

// FIXME It's required some blocks below plugin blocks.
// FIXME https://github.com/gradle/gradle/issues/20131
dependencies {
}