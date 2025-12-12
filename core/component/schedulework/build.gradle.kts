plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component)
    alias(libs.plugins.nlab.android.library.di)
}

android {
    namespace = "com.nlab.reminder.core.component.schedulework"
}

dependencies {
    api(projects.core.data)

    implementation(projects.core.dataDi)

    implementation(libs.androidx.work.runtime.ktx)
}