// Top-level build file where you can add configuration options common to all sub-projects/modules.s

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Deps.androidGradlePlugin)
        classpath(Deps.Kotlin.gradlePlugin)
        classpath("com.android.tools.build:gradle:4.2.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
