plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.jacoco)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.data.impl"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.jsoup)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(projects.testkit)
    testImplementation(projects.core.dataTest)
}