buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Plugin Android Gradle
        classpath("com.android.tools.build:gradle:8.8.1")

        // Safe Args (pour la Navigation)
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
    }
}
