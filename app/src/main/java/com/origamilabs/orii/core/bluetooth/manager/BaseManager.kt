package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.origamilabs.orii.core.bluetooth.BluetoothService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

abstract class BaseManager @Inject constructor(
    @ApplicationContext protected val context: Context,
    protected val bluetoothManager: BluetoothManager,
    protected val bluetoothAdapter: BluetoothAdapter,
    protected val bluetoothService: BluetoothService
) {
    private var isInitialized: Boolean = false

    /**
     * Initialise le manager en appelant la méthode d'initialisation spécifique.
     */
    fun initialize() {
        isInitialized = onInitialize()
    }

    fun isInitialized(): Boolean = isInitialized

    abstract fun close()
    protected abstract fun onInitialize(): Boolean
    protected abstract fun start()
}
