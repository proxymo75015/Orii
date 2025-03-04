package com.origamilabs.orii.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.facebook.drawee.backends.pipeline.Fresco
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.core.bluetooth.BluetoothService
import dagger.hilt.android.HiltAndroidApp

/**
 * Application principale de l’appli ORII.
 */
@HiltAndroidApp
class MainApplication : Application() {

    companion object {
        const val IS_PRODUCTION: Boolean = true
        private const val TAG: String = "MainApplication"
        const val VERSION_NAME: String = "2.2.16"
    }

    // Instance du service principal (AppService), accessible globalement si nécessaire
    var appService: AppService? = null
        private set

    // Connection au service principal
    private val appServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "AppService connecté")
            service?.let {
                appService = (it as AppService.LocalBinder).getService()
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "AppService déconnecté")
            appService = null
        }
    }

    // (Optionnel) Service Bluetooth distinct si utilisé
    private var bluetoothService: BluetoothService? = null

    override fun onCreate() {
        super.onCreate()
        // Initialisation de Fresco (pour gestion d’images, ex: avatars)
        Fresco.initialize(this)

        // **Suppression des initialisations externes** (Firebase, Crashlytics, Analytics, Volley)
        // Plus de FirebaseApp.initializeApp, ni Crashlytics, ni AnalyticsManager ou VolleyManager.

        // Initialisation du AppManager en local (si nécessaire)
        // AppManager.init(applicationContext)  // Par exemple, charger l’état sauvegardé
        // AppManager.start()                   // Démarrer les processus locaux (scan BLE auto, etc.)

        // Démarrage et liaison du service principal de l’application (AppService) pour la gestion BLE
        val appServiceIntent = Intent(this, AppService::class.java)
        bindService(appServiceIntent, appServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Nettoyage : déconnexion du service
        try {
            unbindService(appServiceConnection)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Service déjà déconnecté : ${e.message}")
        }
        // AppManager.close()  // Arrêter proprement les tâches locales si défini
    }

    /**
     * Lie le service Bluetooth (s’il est séparé d’AppService). Exécute le callback une fois connecté.
     */
    fun bindBluetoothService(onConnected: () -> Unit) {
        if (bluetoothService == null) {
            val intent = Intent(this, BluetoothService::class.java)
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    Log.d(TAG, "BluetoothService connecté")
                    bluetoothService = (service as BluetoothService.LocalBinder).getService()
                    onConnected()
                }
                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.d(TAG, "BluetoothService déconnecté")
                    bluetoothService = null
                }
            }, Context.BIND_AUTO_CREATE)
        } else {
            onConnected()
        }
    }
}
