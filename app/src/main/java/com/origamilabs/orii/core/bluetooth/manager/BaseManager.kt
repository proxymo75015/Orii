package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.origamilabs.orii.core.bluetooth.BluetoothService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

/**
 * Classe de base pour tous les managers nécessitant un scope de coroutine.
 */
abstract class AbstractManager : IManager {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun close() {
        Timber.d("Fermeture d'AbstractManager")
        scope.cancel()
    }
}

/**
 * BaseManager fournit l'initialisation commune et la gestion des ressources pour les managers Bluetooth.
 */
abstract class BaseManager @Inject constructor(
    @ApplicationContext protected val context: Context,
    protected val bluetoothManager: BluetoothManager,
    protected val bluetoothAdapter: BluetoothAdapter,
    protected val bluetoothService: BluetoothService
) : AbstractManager() {

    private var initialized: Boolean = false

    override fun initialize(): Boolean {
        initialized = onInitialize()
        return initialized
    }

    fun isInitialized(): Boolean = initialized

    override fun close() {
        onClose()
        super.close()
    }

    /**
     * Initialisation spécifique pour chaque manager.
     */
    protected abstract fun onInitialize(): Boolean

    /**
     * Méthode de nettoyage spécifique pour chaque manager.
     */
    protected abstract fun onClose()

    /**
     * Méthode start() à implémenter par les sous-classes.
     */
    abstract override fun start()
}
