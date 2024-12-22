plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose.component)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage"
}

dependencies {
    api(projects.core.component.uitext)
    implementation(projects.core.foundation)
    implementation(projects.core.kotlin)
    implementation(projects.core.translation)

    testImplementation(projects.core.component.uitextTest)
    testImplementation(projects.testkit)
}