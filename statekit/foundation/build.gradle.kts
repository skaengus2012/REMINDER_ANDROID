plugins {
    alias(libs.plugins.nlab.jvm.library)
    alias(libs.plugins.nlab.jvm.library.jacoco)
}

dependencies {
    api(projects.statekit.core)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.testkit)
}
