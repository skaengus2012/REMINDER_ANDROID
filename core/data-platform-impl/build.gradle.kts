plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.data.platform.impl"
}

dependencies {
    implementation(projects.core.data)
}