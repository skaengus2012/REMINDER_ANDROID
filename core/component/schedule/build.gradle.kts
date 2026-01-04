plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.schedule"
}

dependencies {
    api(projects.core.data)

    implementation(projects.core.annotation)
    implementation(projects.core.dataDi)

    implementation(libs.bundles.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.timber)

    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.testkit)
}