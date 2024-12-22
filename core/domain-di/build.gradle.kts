plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
}

android {
    namespace = "com.nlab.reminder.core.domain.di"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.dataDi)
    implementation(projects.core.kotlinxCoroutine)
}