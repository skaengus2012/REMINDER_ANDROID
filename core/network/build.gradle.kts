plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.network"
}

dependencies {
    api(projects.core.kotlin)
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.jsoup)
}