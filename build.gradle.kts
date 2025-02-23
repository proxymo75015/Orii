// Fichier: build.gradle.kts (niveau projet)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
    }
}
