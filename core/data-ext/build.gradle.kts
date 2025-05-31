plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.data.ext"
}

dependencies {
    api(projects.core.data)
    api(projects.core.uitext)

    testImplementation(projects.core.uitextTest)
}