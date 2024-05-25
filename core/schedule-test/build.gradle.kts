plugins {
    alias(libs.plugins.nlab.jvm.library)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.dataTest)
    implementation(projects.core.schedule)
    implementation(projects.testkit)

    implementation(libs.kotlinx.collections.immutable)
}