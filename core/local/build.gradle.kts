plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nlab.reminder.core.local"

    ksp {
        arg("room.schemaLocation", "${layout.buildDirectory.get()}/schemas")
    }
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.core.kotlin)

    api(libs.androidx.datastore.preferences)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}