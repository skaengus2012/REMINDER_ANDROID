/*
 * Copyright (C) 2022 The N"s lab Open Source Project
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
    alias(libs.plugins.nlab.android.application)
    alias(libs.plugins.nlab.android.application.compose)
    alias(libs.plugins.nlab.android.application.jacoco)
    alias(libs.plugins.nlab.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    kotlin("kapt")
}

android {
    namespace = "com.nlab.reminder"

    defaultConfig {
        applicationId = namespace
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug") // TODO make release key..
        }
    }

    buildTypes.forEach { buildType ->
        if (buildType.isMinifyEnabled) {
            buildType.proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            buildType.proguardFile("proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("release") {
            java.srcDirs("src/release/java")
            res.srcDirs("src/release/res")
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.androidx.compose)
    implementation(projects.core.androidx.fragment)
    implementation(projects.core.androidx.lifecycle)
    implementation(projects.core.androidx.navigationCompose)
    implementation(projects.core.androidx.recyclerview)
    implementation(projects.core.androidx.transition)
    implementation(projects.core.annotation)
    implementation(projects.core.component.usermessage)
    implementation(projects.core.component.usermessageHandle)
    implementation(projects.core.designsystem)
    implementation(projects.core.inject)
    implementation(projects.core.kotlin)
    implementation(projects.core.kotlinxCoroutine)
    implementation(projects.core.statekit)

    implementation(projects.feature.home)

    implementation(libs.afollested.dragselectRecyclerView)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constaintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.coil.kt)
    implementation(libs.google.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    debugImplementation(libs.facebook.flipper)
    debugImplementation(libs.facebook.flipper.leakcanary)
    debugImplementation(libs.facebook.soloader)
    debugImplementation(libs.squeare.leakcanary)

    testImplementation(projects.testkit)
    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.core.scheduleTest)
    androidTestImplementation(projects.core.local)
    androidTestImplementation(projects.testkit)
    androidTestImplementation(libs.javafaker)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}