import org.gradle.configurationcache.extensions.capitalized

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

plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.application")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    jacoco
}

jacoco {
    toolVersion = DependenciesVersions.JACOCO
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        applicationId = "com.nlab.reminder"
        multiDexEnabled = true
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION

        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
        }

        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug") // TODO make release key..
        }
    }

    buildTypes.forEach { buildType ->
        if (buildType.isMinifyEnabled) {
            buildType.proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            buildType.proguardFile("proguard-rules.pro")
        }

        // TODO with Flavor.
        val buildName = buildType.name
        val buildNameCapitalized = buildName.capitalized()
        val taskName = "coverageReport${buildNameCapitalized}"
        val dependOnName = "test${buildNameCapitalized}UnitTest"
        tasks.register<JacocoReport>(taskName) {
            dependsOn(dependOnName)

            group = "reporting"
            description = "Generate Jacoco coverage reports"

            reports {
                html.required.set(true)
                // codecov depends on xml format report
                if (buildType.name == "release") {
                    xml.required.set(true)
                }
            }

            val classFilters = setOf(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/android/**",
                "**/kotlin/**",
                "com/android/**/*.class",
                "**/model/**",
                "**/view/**",
                "**/di/**"
            )

            classDirectories.setFrom(files(
                fileTree("${buildDir}/intermediates/javac/${buildName}/compile${buildNameCapitalized}JavaWithJavac/classes") {
                    setExcludes(classFilters)
                },
                fileTree("${buildDir}/tmp/kotlin-classes/${buildName}") { setExcludes(classFilters) }
            ))
            sourceDirectories.setFrom(file("${projectDir}/src/main/java"))
            executionData.setFrom(files("${buildDir}/jacoco/${dependOnName}.exec"))
        }
    }

    sourceSets {
        getByName("release") {
            java.srcDirs("src/release/java")
            res.srcDirs("src/release/res")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        resources.excludes.add("DebugProbesKt.bin")
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(Dependencies.KOTLIN)
    implementation(Dependencies.KOTLIN_COROUTINE)
    implementation(Dependencies.KOTLIN_COROUTINE_ANDROID)

    implementation(Dependencies.ANDROID_KTX)
    implementation(Dependencies.ANDROID_APPCOMPAT)
    implementation(Dependencies.ANDROID_MATERIAL)
    implementation(Dependencies.ANDROID_CONSTRAINTLAYOUT)
    implementation(Dependencies.ANDROID_RECYCLERVIEW)
    implementation(Dependencies.ANDROID_LIFECYCLE_VIEWMODEL_KTX)
    implementation(Dependencies.ANDROID_LIFECYCLE_RUNTIME_KTX)
    implementation(Dependencies.ANDROID_FRAGMENT)
    implementation(Dependencies.ANDROID_NAVIGATION_FRAGMENT)
    implementation(Dependencies.ANDROID_NAVIGATION_UI)
    implementation(Dependencies.ANDROID_PAGING)

    implementation(Dependencies.GOOGLE_HILT_ANDROID)
    kapt(Dependencies.GOOGLE_HILT_ANDROID_COMPILER)
    implementation(Dependencies.GOGGLE_FLEXBOX)

    testImplementation(Dependencies.TEST_JUNIT)
    testImplementation(Dependencies.TEST_COROUTINES)
    testImplementation(Dependencies.TEST_MOCKITO)
    testImplementation(Dependencies.TEST_MOCKITO_KOTLIN)
    androidTestImplementation(Dependencies.TEST_ANDROID_JUNIT_EXT)
    androidTestImplementation(Dependencies.TEST_ANDROID_JUNIT_ESPRESSO)
    androidTestImplementation(Dependencies.TEST_ANDROID_TEST_RUNNER)
    androidTestImplementation(Dependencies.TEST_ANDROID_TEST_RULES)
}