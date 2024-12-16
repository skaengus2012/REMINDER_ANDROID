plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose.component)
    alias(libs.plugins.nlab.android.library.jacoco)
    alias(libs.plugins.nlab.android.hilt)
}

android {
    namespace = "com.nlab.reminder.core.component.tag"
}

dependencies {
    implementation(projects.core.dataDi)
    implementation(projects.core.domainDi)
    implementation(projects.core.foundationDi)
    implementation(projects.core.translation)

    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.testkit)
}