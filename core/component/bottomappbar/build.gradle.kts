plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component)
}

android {
    namespace = "com.nlab.reminder.core.component.bottomappbar"
}

dependencies {
    implementation(projects.core.translation)
}