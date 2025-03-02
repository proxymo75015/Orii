package com.origamilabs.orii

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Application classe pour initialiser Hilt et configurer l’application
@HiltAndroidApp
class OriiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Toute initialisation globale peut être faite ici (ex: Logger, etc.)
    }
}
