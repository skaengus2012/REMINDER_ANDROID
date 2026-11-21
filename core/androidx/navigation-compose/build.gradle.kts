plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
}

android {
    namespace = "com.nlab.reminder.core.androidx.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)

    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.testkit)
}
