plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.feature.impl)
}

android {
    namespace = "com.nlab.reminder.feature.all.impl"
}

dependencies {
    implementation(projects.core.component.currenttime)
    implementation(projects.core.component.schedule)
    implementation(projects.core.component.schedulelist)
    implementation(projects.core.dataDi)
    implementation(projects.feature.all.api)

    testImplementation(projects.core.component.schedulelistTest)
    testImplementation(projects.core.dataTest)
}