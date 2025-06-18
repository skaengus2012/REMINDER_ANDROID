plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.kotlinx.coroutines.android"
}

dependencies {
    api(projects.core.kotlinx.coroutines)
    api(libs.kotlinx.coroutines.android)
}