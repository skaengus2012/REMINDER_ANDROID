plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.component)
    alias(libs.plugins.nlab.android.library.di)
    alias(libs.plugins.nlab.android.library.jacoco)
}

android {
    namespace = "com.nlab.reminder.core.component.schedulelist"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(projects.core.component.displayformat)
    api(projects.core.dataExt)

    implementation(projects.core.android)
    implementation(projects.core.androidx.fragment)
    implementation(projects.core.androidx.recyclerview)
    implementation(projects.core.annotation)
    implementation(projects.core.component.toolbar)
    implementation(projects.core.dataDi)
    implementation(projects.core.kotlinx.coroutinesAndroid)
    implementation(projects.core.translation)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constaintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.transition.ktx)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.timber)

    testImplementation(projects.core.dataTest)
    testImplementation(projects.core.kotlinTest)
    testImplementation(projects.testkit)
}