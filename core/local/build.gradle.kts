plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nlab.reminder.core.local"

    ksp {
        arg("room.schemaLocation", "${layout.buildDirectory.get()}/schemas")
    }
}

dependencies {
    api(libs.androidx.datastore.preferences)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(projects.core.kotlin)
}