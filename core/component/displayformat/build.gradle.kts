plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.displayformat"
}

dependencies {
    api(projects.core.data)
    implementation(projects.core.annotation)
    implementation(projects.core.dataExt)
    implementation(projects.core.translation)

    implementation(libs.androidx.annotation)

    testImplementation(projects.testkit)
    testImplementation(projects.core.dataTest)

    androidTestImplementation(projects.core.android)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.testkit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}