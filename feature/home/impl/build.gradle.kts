plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.feature.impl)
}

android {
    namespace = "com.nlab.reminder.feature.home.impl"
}

dependencies {
    implementation(projects.core.component.bottomappbar)
    implementation(projects.core.component.tag)
    implementation(projects.core.dataDi)
    implementation(projects.feature.all.api)
    implementation(projects.feature.home.api)

    testImplementation(projects.core.component.tagTest)
    testImplementation(projects.core.dataTest)
}