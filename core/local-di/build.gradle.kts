plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.local.di"
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.core.local)
}