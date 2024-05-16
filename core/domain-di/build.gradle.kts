plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.domain.di"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.dataDi)
    implementation(projects.core.di)
    implementation(projects.core.domain)
    implementation(projects.core.kotlinxCoroutine)
}