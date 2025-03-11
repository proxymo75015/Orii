package com.origamilabs.orii.core.bluetooth.connection

import android.os.Build
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
    callback: Callback,
    private val permissionDelegate: PermissionRequestDelegate? = null
) : ConnectionHandler(context, "Gatt State Handler", callback) {

    companion object {
        private const val REQUEST_BT_PERMISSION = 1001  // Le requestCode explicite
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
            Timber.d("onConnectionStateChange() newState=$newState status=$status")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("onServicesDiscovered() status=$status")
        }

        @Suppress("DEPRECATION") // Ajoute cette ligne précisément ici
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
        mCurrentState = STATE_CONNECTING

        if (mContext.hasBluetoothConnectPermission()) {
            try {
                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback)
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException lors de connectGatt (ligne 90)")
                permissionDelegate?.requestBluetoothPermission(REQUEST_BT_PERMISSION)
                mBluetoothGatt = null
                mIsConnectingGatt = false
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT refusée avant connectGatt (ligne 90)")
            permissionDelegate?.requestBluetoothPermission(REQUEST_BT_PERMISSION)
            mBluetoothGatt = null
            mIsConnectingGatt = false
        }
    }

    override fun disconnect() {
        if (mContext.hasBluetoothConnectPermission()) {
            try {
                mBluetoothGatt?.disconnect()
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException lors de disconnect (ligne 99)")
                permissionDelegate?.requestBluetoothPermission(REQUEST_BT_PERMISSION)
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT refusée avant disconnect (ligne 99)")
            permissionDelegate?.requestBluetoothPermission(REQUEST_BT_PERMISSION)
        }
    }

    @Deprecated("Méthode dépréciée dans la classe parente.")
    override fun close() {
        if (mContext.hasBluetoothConnectPermission()) {
            try {
                mBluetoothGatt?.close()
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException lors de close (ligne 112)")
                permissionDelegate?.requestBluetoothPermission(REQUEST_BT_PERMISSION)
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT refusée avant close (ligne 112)")
            permissionDelegate?.requestBluetoothPermission(REQUEST_BT_PERMISSION)
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
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+ (API 31+)
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Avant Android 12, la permission BLUETOOTH_CONNECT n'existe pas
    }
}
