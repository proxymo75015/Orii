plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Ajout du plugin kapt
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    // Dépendances AndroidX de base
    implementation("androidx.core:core-ktx:1.13.1") // Mise à jour vers la dernière version stable
    implementation("androidx.appcompat:appcompat:1.7.0") // Dernière version
    implementation("com.google.android.material:material:1.12.0") // Dernière version
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Mise à jour vers une version récente

    // Dépendance pour ViewModel et LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7") // Dernière version
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7") // Dernière version

    // Injection de dépendances avec Hilt
    implementation("com.google.dagger:hilt-android:2.51.1") // Mise à jour vers la dernière version
    kapt("com.google.dagger:hilt-android-compiler:2.51.1") // Version synchronisée

    implementation("com.jakewharton.timber:timber:5.0.1")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}