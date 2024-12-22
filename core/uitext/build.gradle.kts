plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.uitext"
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.material3)
}