plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.androidx.transition"
}

dependencies {
    api(libs.androidx.transition.ktx)
}