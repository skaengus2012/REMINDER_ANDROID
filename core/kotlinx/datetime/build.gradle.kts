plugins {
    alias(libs.plugins.nlab.jvm.library)
    alias(libs.plugins.nlab.jvm.library.jacoco)
}

dependencies {
    api(libs.kotlinx.datetime)

    implementation(libs.kotlinx.collections.immutable)

    testImplementation(projects.testkit)
}