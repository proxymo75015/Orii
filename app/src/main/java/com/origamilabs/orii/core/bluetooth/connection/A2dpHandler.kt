package com.origamilabs.orii.core.bluetooth.connection

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Handler pour la connexion A2DP.
 *
 * Cette classe gère la connexion/déconnexion d'un périphérique A2DP en utilisant la réflexion
 * pour invoquer les méthodes "connect", "disconnect" et "setPriority" sur l'API BluetoothA2dp.
 */
class A2dpHandler(
    context: Context,
    callback: ConnectionHandler.Callback
) : ConnectionHandler(context, "A2dp State Handler", callback) {

    companion object {
        private const val TAG = "A2dpHandler"
    }

    private var mA2dpProfile: BluetoothProfile? = null
    private var mIsConnecting: Boolean = false

    private val mA2dpServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
            Log.d(TAG, "Connected to $profile")
            mA2dpProfile = bluetoothProfile
        }

        override fun onServiceDisconnected(profile: Int) {
            mA2dpProfile = null
        }
    }

    init {
        // Obtenir le proxy A2DP via le BluetoothHelper
        BluetoothHelper.getBluetoothAdapter(context)?.getProfileProxy(context, mA2dpServiceListener, BluetoothProfile.A2DP)
    }

    override fun close() {
        // Rien à fermer ici
    }

    override fun isConnecting(): Boolean = mIsConnecting

    override fun getConnectionState(): Int {
        val connectionState = if (mA2dpProfile == null || mDevice == null) {
            0
        } else {
            mA2dpProfile!!.getConnectionState(mDevice)
        }
        // Si l'état est connecté (2) ou déconnecté (0), on réinitialise le flag de connexion.
        if (connectionState == BluetoothProfile.STATE_CONNECTED || connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mIsConnecting = false
        }
        if (!mIsConnecting) {
            return connectionState
        }
        Log.d(TAG, "Disconnecting bug, trying to set connecting")
        return BluetoothProfile.STATE_CONNECTING // généralement 1
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mIsConnecting = true
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            if (getConnectionState() == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Already bonded, connect A2DP directly")
                connectA2dp()
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
        disconnectA2dp()
    }

    private fun connectA2dp() {
        if (mA2dpProfile == null) return

        if (Build.VERSION.SDK_INT < 26) {
            setPriority(mDevice, 100)
        }

        try {
            val connectMethod: Method = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
            connectMethod.isAccessible = true
            connectMethod.invoke(mA2dpProfile, mDevice)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.e(TAG, "Illegal Access! $e")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to invoke connect(BluetoothDevice) method on proxy. $e")
        }
    }

    private fun disconnectA2dp() {
        if (mA2dpProfile == null) return

        if (Build.VERSION.SDK_INT < 26) {
            setPriority(mDevice, 0)
        }

        try {
            val disconnectMethod: Method = BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            disconnectMethod.isAccessible = true
            disconnectMethod.invoke(mA2dpProfile, mDevice)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.e(TAG, "Illegal Access! $e")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to find disconnect(BluetoothDevice) method in BluetoothA2dp proxy.")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to invoke disconnect(BluetoothDevice) method on proxy. $e")
        }
    }

    private fun setPriority(device: BluetoothDevice?, priority: Int) {
        if (mA2dpProfile == null) return

        try {
            val setPriorityMethod: Method = BluetoothA2dp::class.java.getMethod("setPriority", BluetoothDevice::class.java, Int::class.javaPrimitiveType)
            setPriorityMethod.invoke(mA2dpProfile, device, priority)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            e.printStackTrace()
        }
    }
}
