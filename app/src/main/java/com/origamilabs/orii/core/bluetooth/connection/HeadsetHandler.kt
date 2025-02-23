package com.origamilabs.orii.core.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Handler pour la connexion du profil Headset.
 *
 * Gère la connexion et la déconnexion d'un périphérique Bluetooth Headset via des méthodes
 * de réflexion.
 */
class HeadsetHandler(
    context: Context,
    callback: ConnectionHandler.Callback
) : ConnectionHandler(context, "headset State Handler", callback) {

    companion object {
        private const val TAG = "HeadsetHandler"
    }

    private var mHeadsetProfile: BluetoothProfile? = null
    private var mIsConnecting: Boolean = false

    private val mHeadsetServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
            Log.d(TAG, "Connected to $profile")
            mHeadsetProfile = bluetoothProfile
        }

        override fun onServiceDisconnected(profile: Int) {
            mHeadsetProfile = null
        }
    }

    init {
        mIsConnecting = false
        // Obtenir le proxy du profil HEADSET (BluetoothProfile.HEADSET == 1)
        BluetoothHelper.getBluetoothAdapter(mContext)
            ?.getProfileProxy(mContext, mHeadsetServiceListener, BluetoothProfile.HEADSET)
    }

    override fun close() {
        // Aucun comportement à effectuer ici.
    }

    override fun isConnecting(): Boolean = mIsConnecting

    override fun getConnectionState(): Int {
        val connectionState = if (mHeadsetProfile == null || mDevice == null) {
            STATE_DISCONNECTED
        } else {
            mHeadsetProfile!!.getConnectionState(mDevice)
        }
        if (connectionState == STATE_CONNECTED || connectionState == STATE_DISCONNECTED) {
            mIsConnecting = false
        }
        if (!mIsConnecting) {
            return connectionState
        }
        Log.d(TAG, "Disconnecting bug, trying to set connecting")
        return STATE_CONNECTING
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mIsConnecting = true
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            if (getConnectionState() == STATE_DISCONNECTED) {
                Log.d(TAG, "Already bonded, connect headset directly")
                connectHeadset()
                return
            } else {
                mIsConnecting = false
                return
            }
        }
        Log.d(TAG, "Device not bonded, state=${device.bondState}")
        mIsConnecting = false
    }

    override fun disconnect() {
        disconnectHeadset()
    }

    private fun connectHeadset() {
        setPriority(mDevice, 100)
        mHeadsetProfile ?: return
        try {
            val method: Method =
                BluetoothHeadset::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mHeadsetProfile, mDevice)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.e(TAG, "Illegal Access! $e")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to find connect(BluetoothDevice) method in BluetoothHeadset proxy.")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to invoke connect(BluetoothDevice) method on proxy. $e")
        }
    }

    private fun disconnectHeadset() {
        setPriority(mDevice, 0)
        mHeadsetProfile ?: return
        try {
            val method: Method =
                BluetoothHeadset::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mHeadsetProfile, mDevice)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.e(TAG, "Illegal Access! $e")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to find disconnect(BluetoothDevice) method in BluetoothHeadset proxy.")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to invoke disconnect(BluetoothDevice) method on proxy. $e")
        }
    }

    private fun setPriority(device: BluetoothDevice?, priority: Int) {
        if (mHeadsetProfile == null) return
        try {
            val method: Method = BluetoothHeadset::class.java.getMethod(
                "setPriority",
                BluetoothDevice::class.java,
                Int::class.javaPrimitiveType
            )
            method.invoke(mHeadsetProfile, device, priority)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            e.printStackTrace()
        }
    }
}
