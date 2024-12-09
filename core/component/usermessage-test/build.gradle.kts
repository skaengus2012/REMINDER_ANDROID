plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage.test"
}

dependencies {
    implementation(projects.core.component.usermessage)
    implementation(projects.core.kotlin)
    implementation(projects.core.kotlinTest)
    implementation(projects.testkit)
}