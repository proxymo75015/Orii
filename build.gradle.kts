plugins {
    // Plugin Android pour applications (version 8.8.1)
    id("com.android.application") version "8.8.2" apply false

    // Plugin Android pour librairies, si nécessaire
    id("com.android.library") version "8.8.2" apply false

    // Plugin Kotlin pour Android
    kotlin("android") version "1.8.20" apply false

    // Plugin KSP, décommentez si besoin
    id("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
