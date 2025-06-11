plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.data.impl"
}

dependencies {
    implementation(projects.core.annotation)
    implementation(projects.core.dataExt)
    implementation(projects.core.local)
    implementation(projects.core.kotlinx.coroutine)
    implementation(projects.core.network)

    implementation(libs.kotlinx.collections.immutable)

    testImplementation(projects.testkit)
    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.kotlinTest)
}