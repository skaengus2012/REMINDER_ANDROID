plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.annotation.platform.android"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
}