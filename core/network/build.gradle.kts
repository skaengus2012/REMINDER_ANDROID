plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
}

android {
    namespace = "com.nlab.reminder.core.network"
}

dependencies {
    api(projects.core.kotlin)
    api(projects.core.kotlinx.datetime)
    implementation(projects.core.kotlinx.coroutines)

    implementation(libs.google.play.services.time)
    implementation(libs.jsoup)
}