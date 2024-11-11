plugins {
    alias(libs.plugins.nlab.android.library)
}

android {
    namespace = "com.nlab.reminder.core.statekit"
}

dependencies {
    api(projects.statekit.core)
    api(projects.statekit.dsl)
    api(projects.statekit.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
}