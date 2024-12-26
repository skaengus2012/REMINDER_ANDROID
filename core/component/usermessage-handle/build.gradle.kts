plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component.compose)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
    alias(libs.plugins.nlab.android.library.statekit)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage.handle"
}

dependencies {
    implementation(projects.core.annotation)
    implementation(projects.core.component.usermessage)
    implementation(projects.core.kotlinxCoroutine)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(projects.core.uitextTest)
    testImplementation(projects.testkit)
}