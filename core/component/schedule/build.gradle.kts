plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component.compose)
    alias(libs.plugins.nlab.android.library.component.view)
}

android {
    namespace = "com.nlab.reminder.core.component.schedule"
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.component.toolbar)
    implementation(projects.core.translation)
}