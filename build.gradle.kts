// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral() // Ensure Maven Central is included
        maven ("https://jitpack.io" )
    }
    dependencies {
        classpath(libs.android.gradle)
        // Note: Do not place your application dependencies here; they belong in the individual module build.gradle.kts files
    }
}