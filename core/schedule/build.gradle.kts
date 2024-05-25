plugins {
    alias(libs.plugins.nlab.jvm.library)
}

dependencies {
    implementation(projects.core.data)
}