plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.statekit"
}

dependencies {
    api(projects.statekit.core)
    api(projects.statekit.dsl)

    implementation(projects.core.kotlinx.coroutineAndroid)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(projects.testkit)
}