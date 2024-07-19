plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.foundation.impl"
}

dependencies {
    implementation(projects.core.foundation)

    implementation(libs.androidx.collection)
    testImplementation(projects.testkit)
}