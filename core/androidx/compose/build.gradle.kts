plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.androidx.compose"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.datetime)
}