plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.designsystem"
}

dependencies {
    implementation(libs.google.material)
    implementation(libs.androidx.compose.material3)
}