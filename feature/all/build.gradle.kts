plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.feature.view)
}

android {
    namespace = "com.nlab.reminder.feature.all"
}

dependencies {
    implementation(projects.core.component.schedule)
    implementation(projects.core.dataDi)
    
    implementation(projects.core.androidx.recyclerview)
    implementation(libs.androidx.constaintlayout)
}