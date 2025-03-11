plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.origamilabs.orii"
    compileSdk = 34  // Mise à jour vers SDK 34

    defaultConfig {
        applicationId = "com.origamilabs.orii"
        minSdk = 21
        targetSdk = 34  // Mise à jour vers SDK 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // Dépendances Android de base
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")

    // Dépendances pour Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.8")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // Ajout de Timber pour le logging
    implementation("com.jakewharton.timber:timber:5.0.1")
}

// Si vous utilisez des configurations spécifiques pour KAPT
kapt {
    correctErrorTypes = true
}