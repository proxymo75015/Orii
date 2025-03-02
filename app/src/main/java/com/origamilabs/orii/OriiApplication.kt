package com.origamilabs.orii

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OriiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialisations globales éventuelles
    }
}
