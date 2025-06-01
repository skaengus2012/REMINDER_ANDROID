plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.data.ext"
}

dependencies {
    api(projects.core.data)
    api(projects.core.uitext)
    implementation(projects.core.translation)

    testImplementation(projects.core.uitextTest)
}