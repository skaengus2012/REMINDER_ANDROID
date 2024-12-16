plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.component.uitext.test"
}

dependencies {
    implementation(projects.core.component.uitext)
    implementation(projects.core.kotlin)
    implementation(projects.core.kotlinTest)
    implementation(projects.testkit)
}