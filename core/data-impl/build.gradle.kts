plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.data.impl"
}

dependencies {
    implementation(projects.core.annotation)
    implementation(projects.core.data)
    implementation(projects.core.kotlin)
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.jsoup)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(projects.testkit)
    testImplementation(projects.core.dataTest)
}