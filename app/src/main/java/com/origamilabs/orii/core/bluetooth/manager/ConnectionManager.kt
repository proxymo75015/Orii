package com.origamilabs.orii.core.bluetooth.manager

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import timber.log.Timber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IManager {

    private var oriiDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var isConnected: Boolean = false
    private var currentScanCallback: ScanCallback? = null

    companion object {
        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2

        val ORII_SERVICE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        val ORII_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")
    }

    interface Callback {
        fun onA2dpStateChange(prevState: Int, newState: Int)
        fun onGattStateChange(prevState: Int, newState: Int)
        fun onHeadsetStateChange(prevState: Int, newState: Int)
        fun onOriiRemoveBond()
        fun onOriiStateChange(prevState: Int, newState: Int)
    }

    private val callbacks = mutableListOf<Callback>()

    fun addCallback(callback: Callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    private fun notifyOriiStateChange(prevState: Int, newState: Int) {
        callbacks.forEach { it.onOriiStateChange(prevState, newState) }
    }

    private fun notifyGattStateChange(prevState: Int, newState: Int) {
        callbacks.forEach { it.onGattStateChange(prevState, newState) }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun scanAndConnectOriiDevice(timeoutMillis: Long = 10000L): Boolean {
        // Vérifie que les permissions requises sont accordées
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !hasPermission(Manifest.permission.BLUETOOTH_SCAN) ||
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Timber.e("Permissions requises manquantes")
            return false
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter ?: return false
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return false

        notifyOriiStateChange(if (isConnected) STATE_CONNECTED else STATE_DISCONNECTED, STATE_CONNECTING)

        val deviceFound = withTimeoutOrNull(timeoutMillis) {
            suspendCancellableCoroutine<Boolean> { cont ->
                currentScanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        result.device?.let { device ->
                            if (device.name?.contains("ORII") == true) {
                                oriiDevice = device
                                try {
                                    bluetoothLeScanner.stopScan(this)
                                } catch (e: SecurityException) {
                                    Timber.e(e, "Erreur lors de l'arrêt du scan")
                                }
                                currentScanCallback = null
                                if (cont.isActive) cont.resume(true)
                            }
                        }
                    }
                    override fun onScanFailed(errorCode: Int) {
                        if (cont.isActive) cont.resume(false)
                    }
                }
                try {
                    bluetoothLeScanner.startScan(currentScanCallback)
                } catch (e: SecurityException) {
                    cont.resume(false)
                }
                cont.invokeOnCancellation {
                    try {
                        bluetoothLeScanner.stopScan(currentScanCallback)
                    } catch (e: SecurityException) {
                        Timber.e(e, "Erreur lors de l'arrêt du scan en cas d'annulation")
                    }
                    currentScanCallback = null
                }
            }
        } ?: false

        if (!deviceFound || oriiDevice == null) {
            notifyOriiStateChange(STATE_CONNECTING, STATE_DISCONNECTED)
            Timber.e("Appareil ORII non trouvé")
            return false
        }

        val connectionSuccessful = connectToDevice(oriiDevice!!)
        isConnected = connectionSuccessful
        notifyOriiStateChange(STATE_CONNECTING, if (connectionSuccessful) STATE_CONNECTED else STATE_DISCONNECTED)
        return connectionSuccessful
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectToDevice(device: BluetoothDevice): Boolean {
        return suspendCancellableCoroutine { cont ->
            bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        notifyGattStateChange(STATE_CONNECTING, STATE_DISCONNECTED)
                        cont.resume(false)
                        gatt.close()
                        return
                    }
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        notifyGattStateChange(STATE_CONNECTING, STATE_CONNECTED)
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        notifyGattStateChange(STATE_CONNECTED, STATE_DISCONNECTED)
                        cont.resume(false)
                        gatt.close()
                    }
                }
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS && gatt.getService(ORII_SERVICE_UUID) != null) {
                        cont.resume(true)
                    } else {
                        cont.resume(false)
                        gatt.close()
                    }
                }
            })
            cont.invokeOnCancellation {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        val prevState = if (isConnected) STATE_CONNECTED else STATE_DISCONNECTED
        isConnected = false
        notifyOriiStateChange(prevState, STATE_DISCONNECTED)
    }

    fun getBluetoothGatt(): BluetoothGatt? = bluetoothGatt
    fun getBluetoothAdapter(): BluetoothAdapter? = bluetoothAdapter
    fun isOriiConnected(): Boolean = isConnected

    override fun initialize(): Boolean {
        // Initialisations supplémentaires si nécessaire
        return true
    }

    override fun start() {
        // Le démarrage se fait via scanAndConnectOriiDevice ou par une méthode externe.
    }

    override fun close() {
        disconnect()
    }
}
