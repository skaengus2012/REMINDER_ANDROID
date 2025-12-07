plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
}

android {
    namespace = "com.nlab.reminder.core.network"
}

dependencies {
    api(projects.core.kotlin)
    implementation(projects.core.kotlinx.coroutines)

    implementation(libs.coil.kt)
    implementation(libs.coil.kt.svg)
    implementation(libs.google.play.services.time)
    implementation(libs.jsoup)
}