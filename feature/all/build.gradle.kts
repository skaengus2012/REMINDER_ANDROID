plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.compose.feature)
    alias(libs.plugins.nlab.android.library.feature.view)
}

android {
    namespace = "com.nlab.reminder.feature.all"
}

dependencies {
    implementation(projects.core.androidx.recyclerview)
    implementation(projects.core.component.schedule)
    implementation(projects.core.dataDi)

    implementation(libs.androidx.constaintlayout)
}