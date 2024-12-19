plugins {
    alias(libs.plugins.nlab.jvm.library)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.kotlinTest)
    implementation(projects.testkit)
}