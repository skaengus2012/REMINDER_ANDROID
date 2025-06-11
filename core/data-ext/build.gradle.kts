plugins {
    alias(libs.plugins.nlab.jvm.library)
    alias(libs.plugins.nlab.jvm.library.jacoco)
}

dependencies {
    api(projects.core.data)

    testImplementation(projects.testkit)
    testImplementation(projects.core.dataTest)
}