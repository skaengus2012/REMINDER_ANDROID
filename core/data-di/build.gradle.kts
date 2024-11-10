plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.data.di"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.dataImpl)
    implementation(projects.core.foundation)
    implementation(projects.core.foundationDi)
    implementation(projects.core.local)
    implementation(projects.core.localDi)
    implementation(projects.core.network)

    implementation(libs.timber)
}