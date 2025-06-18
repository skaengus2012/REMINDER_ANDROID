plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.androidx.compose"
}

dependencies {
    api(projects.core.kotlinx.datetime)

    implementation(projects.core.designsystem)
    implementation(projects.core.kotlinx.coroutinesAndroid)

    implementation(libs.androidx.compose.material3)
}