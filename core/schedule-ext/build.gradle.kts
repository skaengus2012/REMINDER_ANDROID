plugins {
    alias(libs.plugins.nlab.jvm.library)
    alias(libs.plugins.nlab.jvm.library.jacoco)
}

dependencies {
    implementation(projects.core.dataExt)
    api(projects.core.schedule)

    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.scheduleTest)
    testImplementation(projects.testkit)
}