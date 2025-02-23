package com.origamilabs.orii.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.facebook.drawee.backends.pipeline.Fresco
import com.origamilabs.orii.api.VolleyManager
import com.origamilabs.orii.core.bluetooth.BluetoothService
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.services.AppService
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainApplication : Application() {

    companion object {
        /** Indique si l'application est en production. */
        const val IS_PRODUCTION: Boolean = true

        /** Tag pour les logs. */
        private const val TAG: String = "MainApplication"

        /** Version de l'application. */
        const val VERSION_NAME: String = "2.2.16"

        /**
         * Instance globale de l'application.
         */
        lateinit var instance: MainApplication
            private set
    }

    private var appService: AppService? = null

    // ServiceConnection pour AppService
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

    private var bluetoothService: BluetoothService? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialisation de Fresco pour la gestion des images
        Fresco.initialize(this)

        // Initialisation de Firebase (Crashlytics, Analytics, etc.)
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        // Initialisation du VolleyManager avec le contexte de l'application
        VolleyManager.init(applicationContext)

        // Initialisation et démarrage d'AppManager
        AppManager.init(applicationContext)
        AppManager.start()

        // Initialisation du gestionnaire d'analytics
        AnalyticsManager.init(applicationContext)

        // Démarrage du service d'application (AppService)
        val appServiceIntent = Intent(this, AppService::class.java)
        bindService(appServiceIntent, appServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Fermeture d'AppManager et déconnexion du service
        AppManager.close()
        unbindService(appServiceConnection)
    }

    /**
     * Lie le service Bluetooth. Si le service est déjà connecté, le callback est invoqué immédiatement.
     *
     * @param callback Action à exécuter une fois la connexion établie.
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
     *
     * @param version Version à appliquer pour la mise à jour.
     */
    fun forceUpdateFirmware(version: Int) {
        appService?.forceUpdateFirmwareVersion(version)
    }
}
