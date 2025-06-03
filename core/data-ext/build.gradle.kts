plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.data.ext"
}

dependencies {
    api(projects.core.data)
    api(projects.core.uitext)
    implementation(projects.core.translation)

    implementation(libs.androidx.annotation)

    testImplementation(projects.testkit)
    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.core.uitextTest)
}