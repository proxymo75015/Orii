package com.origamilabs.orii.core.bluetooth.connection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class GattHandler @Inject constructor(
    @ApplicationContext context: Context,
    callback: ConnectionHandler.Callback
) : ConnectionHandler(context, "Gatt State Handler", callback) {

    companion object {
        private const val TAG = "GattHandler"
    }

    private var mBluetoothGatt: BluetoothGatt? = null
    private var mIsClosingGatt: Boolean = false
    private var mIsConnectingGatt: Boolean = false
    private var mIsHandShakeSucceed: Boolean = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mGattCallback = object : BluetoothGattCallback() {
        var disconnectionCount = 0

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.d("onConnectionStateChange() nouvel état: $newState, statut: $status")
            if (newState != BluetoothProfile.STATE_DISCONNECTED) {
                if (newState != BluetoothProfile.STATE_CONNECTED) return
                // Lorsque la connexion est établie, attendre 1 seconde puis démarrer la découverte des services.
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000L)
                    mBluetoothGatt?.let { btGatt ->
                        if (ContextCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            try {
                                Timber.d("Avant découverte des services, état d'appairage: ${btGatt.device.bondState}")
                            } catch (e: SecurityException) {
                                Timber.e(e, "Permission manquante pour accéder à bondState avant découverte")
                            }
                        } else {
                            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour vérifier l'état d'appairage")
                        }
                        Timber.d("Connecté au serveur GATT.")
                        if (ContextCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            try {
                                val discoveryStarted = btGatt.discoverServices()
                                Timber.d("Lancement de la découverte des services: $discoveryStarted")
                            } catch (e: SecurityException) {
                                Timber.e(e, "Permission manquante pour découvrir les services")
                            }
                        } else {
                            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour découvrir les services")
                        }
                        if (ContextCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            try {
                                Timber.d("Après découverte des services, état d'appairage: ${btGatt.device.bondState}")
                            } catch (e: SecurityException) {
                                Timber.e(e, "Permission manquante pour accéder à bondState après découverte")
                            }
                        } else {
                            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour vérifier l'état d'appairage après découverte")
                        }
                    }
                    disconnectionCount = 0
                }
                return
            }
            Timber.d("Déconnecté du serveur GATT.")
            Timber.d("disconnectionCount: $disconnectionCount")
            if (status != 19) {
                if ((status != 133 && status != 66) || disconnectionCount >= 2) {
                    mIsClosingGatt = true
                    scope.launch {
                        disconnect()
                        delay(600L)
                        close()
                        delay(600L)
                        mIsClosingGatt = false
                    }
                    disconnectionCount = 0
                } else {
                    disconnectionCount++
                }
            }
            Timber.d("mIsConnectingGatt: $mIsConnectingGatt")
            mIsConnectingGatt = false
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("onServicesDiscovered() statut=$status")
            if (mBluetoothGatt == null) {
                Timber.d("onServicesDiscovered() mBluetoothGatt est null")
            }
            if (status == 0) {
                mBluetoothGatt?.let { btGatt ->
                    if (ContextCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            Timber.d("onServicesDiscovered() - Avant état d'appairage: ${btGatt.device.bondState}")
                        } catch (e: SecurityException) {
                            Timber.e(e, "Permission manquante pour accéder à bondState avant handshake")
                        }
                    } else {
                        Timber.e("Permission BLUETOOTH_CONNECT non accordée pour vérifier l'état d'appairage")
                    }
                }
                Timber.d("mIsConnectingGatt: $mIsConnectingGatt")
                mIsConnectingGatt = false
                onHandShakeSucceed()
                mBluetoothGatt?.let { btGatt ->
                    if (ContextCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            Timber.d("onServicesDiscovered() - Après état d'appairage: ${btGatt.device.bondState}")
                        } catch (e: SecurityException) {
                            Timber.e(e, "Permission manquante pour accéder à bondState après handshake")
                        }
                    } else {
                        Timber.e("Permission BLUETOOTH_CONNECT non accordée pour vérifier l'état d'appairage après handshake")
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            broadcastUpdate(characteristic)
        }
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mIsConnectingGatt = true
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback)
            } catch (e: SecurityException) {
                Timber.e(e, "Permission manquante lors de l'appel à connectGatt")
                mIsConnectingGatt = false
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour connectGatt.")
            mIsConnectingGatt = false
        }
    }

    override fun disconnect() {
        try {
            mBluetoothGatt?.disconnect()
        } catch (e: SecurityException) {
            Timber.e(e, "Permission manquante lors de l'appel à disconnect")
        }
    }

    @Deprecated("This method is deprecated in the base class")
    override fun close() {
        try {
            mBluetoothGatt?.close()
        } catch (e: SecurityException) {
            Timber.e(e, "Permission manquante lors de l'appel à close")
        }
        mBluetoothGatt = null
    }

    override fun getConnectionState(): Int {
        return when {
            mBluetoothGatt == null -> BluetoothProfile.STATE_DISCONNECTED
            mIsConnectingGatt -> BluetoothProfile.STATE_CONNECTING
            mIsHandShakeSucceed -> BluetoothProfile.STATE_CONNECTED
            else -> BluetoothProfile.STATE_DISCONNECTED
        }
    }

    fun onHandShakeSucceed() {
        mIsHandShakeSucceed = true
        Timber.d("La poignée de main a réussi.")
    }

    private fun broadcastUpdate(characteristic: BluetoothGattCharacteristic?) {
        Timber.d("Mise à jour de la caractéristique reçue: ${characteristic?.uuid}")
    }
}
