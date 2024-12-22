plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.inject)
}

android {
    namespace = "com.nlab.reminder.core.foundation.di"
}

dependencies {
    api(projects.core.foundation)
}