package com.origamilabs.orii.core.bluetooth.connection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Inject

class GattHandler @Inject constructor(
    @ApplicationContext context: Context,
    callback: Callback
) : ConnectionHandler(context, "Gatt State Handler", callback) {

    private var mBluetoothGatt: BluetoothGatt? = null
    private var mIsClosingGatt: Boolean = false
    private var mIsConnectingGatt: Boolean = false
    private var mIsHandShakeSucceed: Boolean = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mGattCallback = object : BluetoothGattCallback() {
        var disconnectionCount = 0
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.d("onConnectionStateChange() newState=$newState status=$status")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("onServicesDiscovered() status=$status")
        }

        @Deprecated(
            message = "Deprecated in API 33. Use onCharacteristicChanged with ByteArray parameter.",
            replaceWith = ReplaceWith("onCharacteristicChanged(gatt, characteristic, value)"),
            level = DeprecationLevel.WARNING
        )
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Timber.d("onCharacteristicChanged() characteristic=${characteristic.uuid}")
            broadcastUpdate(characteristic)
        }

        @RequiresApi(33)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Timber.d("onCharacteristicChanged() value=${value.contentToString()}, characteristic=${characteristic.uuid}")
            @Suppress("DEPRECATION")
            characteristic.value = value
            broadcastUpdate(characteristic)
        }
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mIsConnectingGatt = true
        if (mContext.hasBluetoothConnectPermission()) {
            try {
                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback)
            } catch (e: SecurityException) {
                Timber.e(e, "Permission BLUETOOTH_CONNECT manquante")
                mIsConnectingGatt = false
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour connectGatt.")
            mIsConnectingGatt = false
        }
    }

    override fun disconnect() {
        runCatching {
            mBluetoothGatt?.disconnect()
        }.onFailure {
            Timber.e(it, "Erreur lors de la déconnexion")
        }
    }

    @Deprecated("Méthode dépréciée dans la classe parente.")
    override fun close() {
        runCatching {
            mBluetoothGatt?.close()
        }.onFailure {
            Timber.e(it, "Erreur lors de la fermeture du GATT")
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

    private fun broadcastUpdate(characteristic: BluetoothGattCharacteristic?) {
        Timber.d("Mise à jour de la caractéristique : ${characteristic?.uuid}")
    }
}

// Extension function pour vérifier la permission BLUETOOTH_CONNECT
private fun Context.hasBluetoothConnectPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
}
