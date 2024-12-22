plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.androidx.navigation.compose"
}

dependencies {
    api(libs.androidx.navigation.compose)
}