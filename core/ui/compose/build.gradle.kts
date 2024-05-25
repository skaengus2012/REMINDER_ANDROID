plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.ui.compose"
}

dependencies {
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.androidx.compose.foundation)
}