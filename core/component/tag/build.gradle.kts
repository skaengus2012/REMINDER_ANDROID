plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose.component)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.tag"
}

dependencies {
    implementation(projects.core.annotation)
    implementation(projects.core.component.displayformat)
    implementation(projects.core.dataDi)
    implementation(projects.core.translation)

    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.testkit)
}