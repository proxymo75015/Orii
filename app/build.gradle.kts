plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin") // Safe Args pour Navigation Component
    id("com.google.gms.google-services")   // Firebase
    id("com.google.firebase.crashlytics")  // Crashlytics
    id("kotlin-kapt")                      // Pour DataBinding et Room
    id("kotlin-parcelize")                 // Pour Parcelable dans Safe Args
}

android {
    namespace = "com.origamilabs.orii"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.origamilabs.orii"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true  // Active DataBinding
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "crashlytics-rules.pro" // Pour Crashlytics
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    // Firebase BOM (gestion automatique des versions compatibles)
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // Firebase
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Facebook SDK (Core, Login et Share)
    implementation("com.facebook.android:facebook-android-sdk:16.1.3")
    implementation("com.facebook.android:facebook-login:16.1.3")
    implementation("com.facebook.android:facebook-share:16.1.3")

    // Navigation Component (avec Safe Args activé)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Volley pour les requêtes réseau
    implementation("com.android.volley:volley:1.2.1")

    // Gson pour la sérialisation/désérialisation JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Room Database (base de données locale)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Dépendances AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // DataBinding (utile si DataBinding est activé dans buildFeatures)
    implementation("androidx.databinding:databinding-runtime:8.1.0")
    kapt("androidx.databinding:compiler:8.1.0")

    // Fresco (incluant com.facebook.drawee)
    implementation("com.facebook.fresco:fresco:2.6.0")
}
