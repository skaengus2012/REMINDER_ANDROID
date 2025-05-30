plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.component.usermessage.test"
}

dependencies {
    api(projects.core.uitextTest)

    implementation(projects.core.component.usermessage)
    implementation(projects.testkit)
}