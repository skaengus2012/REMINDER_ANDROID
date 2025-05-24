plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose.feature)
}

android {
    namespace = "com.nlab.reminder.feature.home"
}

dependencies {
    implementation(projects.core.component.tag)
    implementation(projects.core.dataDi)

    testImplementation(projects.core.component.tagTest)
    testImplementation(projects.core.dataTest)
}