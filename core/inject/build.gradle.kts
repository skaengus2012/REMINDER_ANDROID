plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.inject"
}

dependencies {
    implementation(projects.core.kotlinx.coroutine)
}