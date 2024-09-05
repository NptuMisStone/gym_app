pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
// In settings.gradle.kts, add the Google repository
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google() // Add Google repository here
        mavenCentral()
        maven ("https://jitpack.io")
    }
}

rootProject.name = "gym_app"
include(":app")
 