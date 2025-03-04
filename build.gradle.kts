buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Plugin Android Gradle
        classpath("com.android.tools.build:gradle:8.8.1")
        // Plugin Kotlin Gradle
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
        // Plugin Hilt pour Android
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
