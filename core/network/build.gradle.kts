plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
}

android {
    namespace = "com.nlab.reminder.core.network"
}

dependencies {
    api(projects.core.kotlin)
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.jsoup)
}