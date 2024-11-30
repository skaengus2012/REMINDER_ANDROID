plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage"
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.material3)
}