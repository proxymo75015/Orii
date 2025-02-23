pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.gms.google-services" -> useModule("com.google.gms:google-services:4.3.15")
                "com.google.firebase.crashlytics" -> useModule("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Assure que seuls les dépôts définis ici sont utilisés
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Orii"
include(":app")
