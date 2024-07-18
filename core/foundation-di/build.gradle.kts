plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.foundation.di"
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.core.foundation)
    implementation(projects.core.foundationImpl)
}