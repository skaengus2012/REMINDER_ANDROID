plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.currenttime"
}

dependencies {
    implementation(projects.core.dataDi)
    implementation(projects.core.translation)

    testImplementation(projects.core.dataTest)
    testImplementation(projects.testkit)
}