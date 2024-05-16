plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.designsystem.compose"
}

dependencies {
    implementation(libs.androidx.compose.material3)
}