package com.origamilabs.orii.core.bluetooth

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.core.bluetooth.manager.RouteManager
import com.origamilabs.orii.core.bluetooth.manager.ScanManager

@AndroidEntryPoint
class BluetoothService : Service() {

    companion object {
        private const val TAG = "BluetoothService"
    }

    // Binder pour lier le service à un client
    private val mBinder = LocalBinder()

    // Scope dédié aux opérations asynchrones du service.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Injection des managers via Hilt
    @Inject lateinit var scanManager: ScanManager
    @Inject lateinit var connectionManager: ConnectionManager
    @Inject lateinit var routeManager: RouteManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service créé")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        // Lancement d'une coroutine pour initialiser les managers de manière asynchrone
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!scanManager.isInitialized()) {
                        scanManager.initialize(this@BluetoothService)
                    }
                    if (!connectionManager.isInitialized()) {
                        connectionManager.initialize(this@BluetoothService)
                    }
                    if (!routeManager.isInitialized()) {
                        routeManager.initialize(this@BluetoothService)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de l'initialisation des managers", e)
                }
            }
        }
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        serviceScope.cancel() // Annule les tâches asynchrones en cours
        super.onDestroy()
        Log.d(TAG, "Service détruit")
    }

    /**
     * Binder local permettant aux clients d'accéder au service.
     */
    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }
}
