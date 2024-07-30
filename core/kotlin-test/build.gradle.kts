plugins {
    alias(libs.plugins.nlab.jvm.library)
}

dependencies {
    implementation(projects.core.kotlin)
    implementation(projects.testkit)
}