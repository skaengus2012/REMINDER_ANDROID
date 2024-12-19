plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.component.tag.test"
}

dependencies {
    implementation(projects.core.component.tag)
    implementation(projects.core.data)
    implementation(projects.core.dataTest)
    implementation(projects.core.kotlin)
    implementation(projects.core.kotlinTest)
    implementation(projects.testkit)
}