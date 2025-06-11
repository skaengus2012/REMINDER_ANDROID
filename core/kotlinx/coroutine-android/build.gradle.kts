plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.kotlinx.coroutine.android"
}

dependencies {
    api(projects.core.kotlinx.coroutine)
    api(libs.kotlinx.coroutines.android)
}