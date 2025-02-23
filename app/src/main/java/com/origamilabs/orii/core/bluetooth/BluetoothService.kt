package com.origamilabs.orii.core.bluetooth

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.core.bluetooth.manager.RouteManager
import com.origamilabs.orii.core.bluetooth.manager.ScanManager

/**
 * Service Bluetooth permettant d'initialiser et de gérer les managers Bluetooth.
 *
 * Ce service renvoie un binder permettant à d'autres composants de récupérer l'instance de ce service.
 */
class BluetoothService : Service() {

    companion object {
        private const val TAG = "BluetoothService"
    }

    // Binder pour lier le service à un client
    private val mBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        // Initialisation spécifique si nécessaire
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libération des ressources si nécessaire
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        // Initialisation des managers si ce n'est pas déjà fait
        if (!ScanManager.getInstance().isInitialized()) {
            ScanManager.getInstance().initialize(this)
        }
        if (!ConnectionManager.getInstance().isInitialized()) {
            ConnectionManager.getInstance().initialize(this)
        }
        if (!RouteManager.getInstance().isInitialized()) {
            RouteManager.getInstance().initialize(this)
        }
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    /**
     * Binder local permettant aux clients d'accéder au service.
     */
    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }
}
