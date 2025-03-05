plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.origamilabs.orii"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    // Pour l'utilisation de l'extension viewModels()
    implementation("androidx.activity:activity-ktx:1.10.1")

    // Dagger Hilt pour l'injection de dépendances
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.46.1")

    // Timber pour le logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Kotlin Coroutines pour la gestion asynchrone
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
