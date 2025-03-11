package com.origamilabs.orii.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.core.bluetooth.BluetoothService
import com.origamilabs.orii.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApplication : Application() {

    companion object {
        const val IS_PRODUCTION: Boolean = true
        const val VERSION_NAME: String = "3.0.00"
    }

    var appService: AppService? = null
        private set

    private val appServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("AppService connecté")
            service?.let {
                appService = (it as AppService.LocalBinder).getService()
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("AppService déconnecté")
            appService = null
        }
    }

    private var bluetoothService: BluetoothService? = null

    override fun onCreate() {
        super.onCreate()
        // Initialisation de Timber (DebugTree en mode debug)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Suppression de Fresco puisque nous utilisons Glide pour le chargement d'images

        val appServiceIntent = Intent(this, AppService::class.java)
        bindService(appServiceIntent, appServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            unbindService(appServiceConnection)
        } catch (e: IllegalArgumentException) {
            Timber.w("Service déjà déconnecté : ${e.message}")
        }
    }

    fun bindBluetoothService(onConnected: () -> Unit) {
        if (bluetoothService == null) {
            val intent = Intent(this, BluetoothService::class.java)
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    Timber.d("BluetoothService connecté")
                    bluetoothService = (service as BluetoothService.LocalBinder).getService()
                    onConnected()
                }
                override fun onServiceDisconnected(name: ComponentName?) {
                    Timber.d("BluetoothService déconnecté")
                    bluetoothService = null
                }
            }, Context.BIND_AUTO_CREATE)
        } else {
            onConnected()
        }
    }
}
