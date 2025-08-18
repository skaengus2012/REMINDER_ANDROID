plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.statekit.androidx.lifecycle"
}

dependencies {
    api(projects.statekit.foundation)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(projects.testkit)
}