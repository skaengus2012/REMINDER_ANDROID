plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)
    defaultConfig {
        applicationId = "com.nlab.practice"
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(Dependencies.KOTLIN)
    implementation(Dependencies.ANDROID_KTX)
    implementation(Dependencies.ANDROID_APPCOMPAT)
    implementation(Dependencies.ANDROID_MATERIAL)
    implementation(Dependencies.ANDROID_CONSTRAINT_LAYOUT)
    testImplementation(Dependencies.TEST_JUNIT)
    androidTestImplementation(Dependencies.TEST_ANDROID_JUNIT_EXT)
    androidTestImplementation(Dependencies.TEST_ANDROID_JUNIT_ESPRESSO)
}