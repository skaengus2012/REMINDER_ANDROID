plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component.compose)
}

android {
    namespace = "com.nlab.reminder.core.component.toolbar"
}

dependencies {
    implementation(projects.core.translation)
}