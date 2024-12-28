plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.androidx.fragment.compose"
}

dependencies {
    api(libs.androidx.fragment.compose)
    implementation(libs.androidx.compose.ui)
}