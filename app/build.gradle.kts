plugins {
    // Plugins Android/Kotlin de base
    id("com.android.application")
    kotlin("android")

    // KSP pour la génération de code (ex: Room)
    id("com.google.devtools.ksp")

    // Hilt (qui s’appuie encore sur KAPT)
    id("com.google.dagger.hilt.android")

    // KAPT pour Hilt (et autres libs qui utilisent encore KAPT)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.origamilabs.orii"
    compileSdk = 34  // Dernier SDK Android

    defaultConfig {
        applicationId = "com.origamilabs.orii"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Test runner si besoin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Activation des features selon vos besoins
    buildFeatures {
        viewBinding = true
        dataBinding = true
        // Si Jetpack Compose :
        // compose = true
    }

    /*
    // Configurez la version du compilateur Compose si vous l'utilisez
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.5"
    }
    */

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Version du JDK ciblée
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Version Kotlin ciblée
    kotlinOptions {
        jvmTarget = "17"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    // ————————————
    // Bibliothèques Android de base
    // ————————————
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")

    // ————————————
    // Hilt
    // ————————————
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // ————————————
    // Navigation
    // ————————————
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // ————————————
    // Logging
    // ————————————
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ————————————
    // ROOM (avec KSP)
    // ————————————
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ————————————
    // DataStore (pour remplacer SharedPreferences)
    // ————————————
    implementation("androidx.datastore:datastore-preferences:1.1.3")

    // ————————————
    // Gson (pour la sérialisation JSON dans DataStore)
    // ————————————
    implementation("com.google.code.gson:gson:2.12.1")
}
