plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.uitext.test"
}

dependencies {
    implementation(projects.core.uitext)
    implementation(projects.core.kotlin)
    implementation(projects.core.kotlinTest)
    implementation(projects.testkit)
}