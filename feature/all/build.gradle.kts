plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.feature)
}

android {
    namespace = "com.nlab.reminder.feature.all"
}

dependencies {
    implementation(projects.core.component.schedulelist)
    implementation(projects.core.dataDi)
}