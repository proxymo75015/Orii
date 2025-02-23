package com.origamilabs.orii.core.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import com.origamilabs.orii.core.bluetooth.connection.ConnectionHandler.Companion.STATE_CONNECTED
import com.origamilabs.orii.core.bluetooth.connection.ConnectionHandler.Companion.STATE_DISCONNECTED
import com.origamilabs.orii.core.bluetooth.connection.ConnectionHandler.Companion.STATE_CONNECTING
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Gestionnaire de connexion GATT pour les périphériques Bluetooth.
 *
 * Ce handler gère la connexion, la déconnexion, le rafraîchissement et la mise à jour de l'état de la connexion GATT.
 */
class GattHandler(
    context: Context,
    callback: ConnectionHandler.Callback
) : ConnectionHandler(context, "Gatt State Handler", callback) {

    companion object {
        private const val TAG = "GattHandler"
    }

    private var mBluetoothGatt: BluetoothGatt? = null
    private val mGattCallback: BluetoothGattCallback
    private var mIsClosingGatt: Boolean = false
    private var mIsConnectingGatt: Boolean = false
    private var mIsHandShakeSucceed: Boolean = false

    init {
        mIsHandShakeSucceed = false
        mIsConnectingGatt = false
        mIsClosingGatt = false
        mGattCallback = object : BluetoothGattCallback() {
            var disconnectionCount = 0

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d(TAG, "onConnectionStateChange() new state:$newState, new status:$status")
                if (newState != BluetoothProfile.STATE_DISCONNECTED) {
                    if (newState != BluetoothProfile.STATE_CONNECTED) return
                    // Lorsque la connexion est établie, attendre 1 seconde puis démarrer la découverte des services.
                    mMainHandler.postDelayed({
                        mBluetoothGatt?.let { btGatt ->
                            Log.d(TAG, "Before discoverServices Bond state: ${btGatt.device.bondState}")
                            Log.d(TAG, "Connected to GATT server.")
                            Log.d(TAG, "Attempting to start service discovery: ${btGatt.discoverServices()}")
                            Log.d(TAG, "After discoverServices Bond state: ${btGatt.device.bondState}")
                        }
                        disconnectionCount = 0
                    }, 1000L)
                    return
                }
                Log.d(TAG, "Disconnected from GATT server.")
                Log.d(TAG, "disconnectionCount: $disconnectionCount")
                CommandManager.getInstance().close()
                if (status != 19) {
                    if ((status != 133 && status != 66) || disconnectionCount >= 2) {
                        try {
                            mIsClosingGatt = true
                            disconnect()
                            Thread.sleep(600L)
                            close()
                            Thread.sleep(600L)
                            mIsClosingGatt = false
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        disconnectionCount = 0
                    } else {
                        disconnectionCount++
                    }
                }
                Log.d(TAG, "mIsConnectingGatt: $mIsConnectingGatt")
                mIsConnectingGatt = false
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                Log.d(TAG, "onServicesDiscovered() status=$status")
                if (mBluetoothGatt == null) {
                    Log.d(TAG, "onServicesDiscovered() the mBluetoothGatt is null")
                }
                if (status == 0) {
                    mBluetoothGatt?.let { btGatt ->
                        Log.d(TAG, "onServicesDiscovered()- Before bond state: ${btGatt.device.bondState}")
                    }
                    Log.d(TAG, "mIsConnectingGatt: $mIsConnectingGatt")
                    mIsConnectingGatt = false
                    onHandShakeSucceed()
                    mBluetoothGatt?.let { btGatt ->
                        Log.d(TAG, "onServicesDiscovered()- After bond state: ${btGatt.device.bondState}")
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
    }

    fun onHandShakeSucceed() {
        mIsHandShakeSucceed = true
        
