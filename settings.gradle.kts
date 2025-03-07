pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useVersion("1.8.20-1.0.11")  // Compatible avec Kotlin 1.8.20
            }
            if (requested.id.id == "com.google.dagger.hilt.android") {
                useVersion("2.51.1")  // Version explicite pour Hilt
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Orii"
include(":app")
