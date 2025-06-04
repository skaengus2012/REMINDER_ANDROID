plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.data.format"
}

dependencies {
    api(projects.core.data)
    implementation(projects.core.uitext)
    implementation(projects.core.translation)

    implementation(libs.androidx.annotation)
}