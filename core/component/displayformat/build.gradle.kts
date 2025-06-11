plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.component.displayformat"
}

dependencies {
    api(projects.core.data)
    implementation(projects.core.dataExt)
    implementation(projects.core.uitext)
    implementation(projects.core.translation)

    implementation(libs.androidx.annotation)

    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.testkit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}