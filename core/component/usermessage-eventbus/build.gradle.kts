plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose.component)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
    alias(libs.plugins.nlab.android.library.statekit)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage.eventbus"
}

dependencies {
    implementation(projects.core.annotation)
    implementation(projects.core.component.usermessage)
    implementation(projects.core.kotlinx.coroutines)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(projects.core.component.usermessageTest)
    testImplementation(projects.testkit)
}