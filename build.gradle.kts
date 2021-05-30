buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${DependenciesVersions.GRADLE}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${DependenciesVersions.KOTLIN}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}