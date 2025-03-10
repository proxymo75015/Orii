package com.origamilabs.orii.core.bluetooth

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.core.bluetooth.manager.ScanManager

@AndroidEntryPoint
class BluetoothService @Inject constructor() : Service() {

    @Inject lateinit var scanManager: ScanManager
    @Inject lateinit var connectionManager: ConnectionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        Timber.d("BluetoothService créé")
    }

    override fun onBind(intent: Intent?): IBinder {
        Timber.d("BluetoothService onBind appelé")
        initializeManagers()
        return binder
    }

    private fun initializeManagers() {
        serviceScope.launch {
            try {
                if (!::scanManager.isInitialized) {
                    Timber.e("scanManager n'est pas initialisé par Hilt.")
                } else {
                    scanManager.initialize()
                }

                if (!::connectionManager.isInitialized) {
                    Timber.e("connectionManager n'est pas initialisé par Hilt.")
                } else {
                    connectionManager.initialize()
                }

                Timber.d("Managers initialisés avec succès")
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors de l'initialisation des managers")
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("BluetoothService onUnbind appelé")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Timber.d("BluetoothService détruit")
        super.onDestroy()
    }
}
