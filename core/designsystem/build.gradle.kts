plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.designsystem"
}

dependencies {
    implementation(libs.google.material)
}