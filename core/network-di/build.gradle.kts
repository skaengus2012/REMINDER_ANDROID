plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
}

android {
    namespace = "com.nlab.reminder.core.network.di"
}

dependencies {
    api(projects.core.network)

    implementation(libs.timber)
}