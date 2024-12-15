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

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

/**
 * @author Doohyun
 */
private val selectedJavaVersion: JavaVersion get() = JavaVersion.VERSION_17
private val selectedJvmTarget: JvmTarget get() = JvmTarget.JVM_17

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileOptions {
            sourceCompatibility = selectedJavaVersion
            targetCompatibility = selectedJavaVersion
        }

        packaging {
            // guide in kotlin coroutine
            // https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            resources.excludes.add("DebugProbesKt.bin")
        }
    }

    configureKotlin<KotlinAndroidProjectExtension> {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        )
    }
}

internal fun Project.configureKotlinJvm() {
    java {
        sourceCompatibility = selectedJavaVersion
        targetCompatibility = selectedJavaVersion
    }

    configureKotlin<KotlinJvmProjectExtension>()
}

private inline fun <reified T : KotlinTopLevelExtension> Project.configureKotlin(
    crossinline block: KotlinJvmCompilerOptions.() -> Unit = {}
) = configure<T> {
    // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
    val warningsAsErrors: String? by project
    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> TODO("Unsupported project extension $this ${T::class}")
    }.apply {
        jvmTarget.set(selectedJvmTarget)
        allWarningsAsErrors.set(warningsAsErrors.toBoolean())
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlin.Experimental",
            /**
             * Remove this args after Phase 3.
             * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-consistent-copy-visibility/#deprecation-timeline
             *
             * Deprecation timeline
             * Phase 3. (Supposedly Kotlin 2.2 or Kotlin 2.3).
             * The default changes.
             * Unless ExposedCopyVisibility is used, the generated 'copy' method has the same visibility as the primary constructor.
             * The binary signature changes. The error on the declaration is no longer reported.
             * '-Xconsistent-data-class-copy-visibility' compiler flag and ConsistentCopyVisibility annotation are now unnecessary.
             */
            "-Xconsistent-data-class-copy-visibility"
        )
        block()
    }
}
