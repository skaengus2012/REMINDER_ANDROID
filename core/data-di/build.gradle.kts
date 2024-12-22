plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.inject)
}

android {
    namespace = "com.nlab.reminder.core.data.di"
}

dependencies {
    api(projects.core.data)
    implementation(projects.core.dataImpl)
    implementation(projects.core.dataPlatformImpl)
    implementation(projects.core.localDi)
    implementation(projects.core.network)

    implementation(libs.timber)
}