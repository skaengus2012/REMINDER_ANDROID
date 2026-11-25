plugins {
    alias(libs.plugins.nlab.android.library)
    alias(libs.plugins.nlab.android.library.feature.impl)
}

android {
    namespace = "com.nlab.reminder.feature.all.impl"
}

dependencies {
    implementation(projects.core.component.currenttime)
    implementation(projects.core.component.schedule)
    implementation(projects.core.component.schedulelist)
    implementation(projects.core.dataDi)
<<<<<<< HEAD:feature/all/build.gradle.kts

    testImplementation(projects.core.component.schedulelistTest)
    testImplementation(projects.core.dataTest)
||||||| c64fd2e2:feature/all/build.gradle.kts
=======
    implementation(projects.feature.all.api)
>>>>>>> origin/master:feature/all/impl/build.gradle.kts
}