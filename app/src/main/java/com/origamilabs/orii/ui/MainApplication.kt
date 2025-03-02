package com.origamilabs.orii.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.origamilabs.orii.api.VolleyManager
import com.origamilabs.orii.core.bluetooth.BluetoothService
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.services.AppService
import dagger.hilt.android.HiltAndroidApp

/**
 * Application principale.
 */
@HiltAndroidApp // Nécessaire pour que Hilt fonctionne dans toute l'application
class MainApplication : Application() {

    companion object {
        /** Indique si l'application est en production. */
        const val IS_PRODUCTION: Boolean = true

        /** Tag pour les logs. */
        private const val TAG: String = "MainApplication"

        /** Version de l'application. */
        const val VERSION_NAME: String = "2.2.16"
    }

    // Service d'application (lié via bindService)
    var appService: AppService? = null
        private set

    // Connexion pour AppService
    private val appServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            Log.d(TAG, "AppService connecté")
            service?.let {
                appService = (it as AppService.LocalBinder).getService()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(TAG, "AppService déconnecté")
            appService = null
        }
    }

    // Service Bluetooth (même principe)
    private var bluetoothService: BluetoothService? = null

    override fun onCreate() {
        super.onCreate()

        // Initialisation Fresco
        Fresco.initialize(this)

        // Initialisation Firebase
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        // Initialisation Volley
        VolleyManager.init(applicationContext)

        // Initialisation et démarrage d'AppManager
        AppManager.init(applicationContext)
        AppManager.start()

        // Initialisation du gestionnaire d'analytics
        AnalyticsManager.init(applicationContext)

        // Démarrage et liaison du service d'application
        val appServiceIntent = Intent(this, AppService::class.java)
        bindService(appServiceIntent, appServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Nettoyage d'AppManager et déconnexion du service
        AppManager.close()
        unbindService(appServiceConnection)
    }

    /**
     * Lie le service Bluetooth. Si déjà connecté, exécute immédiatement le callback.
     */
    fun bindBluetoothService(callback: () -> Unit) {
        if (bluetoothService == null) {
            val bluetoothIntent = Intent(this, BluetoothService::class.java)
            bindService(bluetoothIntent, object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
                    Log.d(TAG, "BluetoothService connecté")
                    service?.let {
                        bluetoothService = (it as BluetoothService.LocalBinder).getService()
                    }
                    callback()
                }

                override fun onServiceDisconnected(componentName: ComponentName?) {
                    Log.d(TAG, "BluetoothService déconnecté")
                    bluetoothService = null
                }
            }, Context.BIND_AUTO_CREATE)
        } else {
            callback()
        }
    }

    /**
     * Force la mise à jour du firmware via AppService.
     */
    fun forceUpdateFirmware(version: Int) {
        appService?.forceUpdateFirmwareVersion(version)
    }
}
