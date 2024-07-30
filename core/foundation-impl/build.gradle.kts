plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.foundation.impl"
}

dependencies {
    implementation(projects.core.foundation)
}