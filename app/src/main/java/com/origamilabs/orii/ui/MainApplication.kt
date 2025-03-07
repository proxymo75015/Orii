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

@HiltAndroidApp
class MainApplication : Application() {

    companion object {
        const val IS_PRODUCTION: Boolean = true
        private const val TAG: String = "MainApplication"
        const val VERSION_NAME: String = "2.2.16"
    }

    var appService: AppService? = null
        private set

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

    private var bluetoothService: BluetoothService? = null

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)

        val appServiceIntent = Intent(this, AppService::class.java)
        bindService(appServiceIntent, appServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            unbindService(appServiceConnection)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Service déjà déconnecté : ${e.message}")
        }
    }

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