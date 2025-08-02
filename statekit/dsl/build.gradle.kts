plugins {
    alias(libs.plugins.nlab.jvm.library)
    alias(libs.plugins.nlab.jvm.library.jacoco)
}

dependencies {
    implementation(projects.statekit.core)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.statekit.test)
    testImplementation(projects.testkit)
}