plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.inject)
}

android {
    namespace = "com.nlab.reminder.core.local.di"
}

dependencies {
    api(projects.core.local)
}