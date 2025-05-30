plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage"
}

dependencies {
    api(projects.core.uitext)
    implementation(projects.core.annotation)
    implementation(projects.core.kotlin)
    implementation(projects.core.translation)

    testImplementation(projects.core.uitextTest)
    testImplementation(projects.testkit)
}