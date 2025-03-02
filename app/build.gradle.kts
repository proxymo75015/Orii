plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    id("com.google.dagger.hilt.android") version "2.48"
}

android {
    namespace = "com.origamilabs.orii"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.origamilabs.orii"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        dataBinding = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildToolsVersion = "35.0.1"

    packaging {
        resources {
            excludes += setOf("values/values.xml")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Firebase BOM et dépendances associées
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Google Play Services
    implementation("com.google.android.gms:play-services-measurement:22.2.0")
    implementation("com.google.android.gms:play-services-measurement-api:22.2.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:18.0.2")
    implementation("com.facebook.android:facebook-login:18.0.2")
    implementation("com.facebook.android:facebook-share:18.0.2")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.8")

    // Réseaux et JSON
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.12.1")

    // Room (avec KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // UI et Material Components
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.facebook.fresco:fresco:3.6.0")

    // Hilt (avec KSP)
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    // Material Icons Extended pour les icônes vectorielles
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
}

configurations.all {
    resolutionStrategy {
        // Configuration optionnelle pour forcer une version spécifique si nécessaire
        // force("com.origamilabs.orii:module-name:version")
    }
}
