plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.uitext"
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.material3)

    androidTestImplementation(projects.testkit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}