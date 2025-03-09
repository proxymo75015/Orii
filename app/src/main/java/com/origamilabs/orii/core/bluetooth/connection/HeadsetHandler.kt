package com.origamilabs.orii.core.bluetooth.connection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.inject.Inject

@SuppressLint("MissingPermission")
class HeadsetHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    callback: Callback
) : ConnectionHandler(context, "Headset State Handler", callback) {

    companion object {
        private const val TAG = "HeadsetHandler"
    }

    private var mHeadsetProfile: BluetoothProfile? = null
    private var mIsConnecting: Boolean = false

    private val mHeadsetServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
            Timber.d("Connecté au profil $profile")
            mHeadsetProfile = bluetoothProfile
        }
        override fun onServiceDisconnected(profile: Int) {
            mHeadsetProfile = null
        }
    }

    init {
        mIsConnecting = false
        if (context.hasBluetoothConnectPermission()) {
            try {
                BluetoothHelper.getBluetoothAdapter(context)
                    ?.getProfileProxy(context, mHeadsetServiceListener, BluetoothProfile.HEADSET)
            } catch (e: SecurityException) {
                Timber.e(e, "Permission refusée lors de l'obtention du proxy du profil Headset.")
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée. Impossible d'obtenir le proxy du profil Headset.")
        }
    }

    override fun close() {
        // Aucun comportement à effectuer ici.
    }

    override fun getConnectionState(): Int {
        if (!context.hasBluetoothConnectPermission()) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée. Impossible de récupérer l'état de connexion.")
            return STATE_DISCONNECTED
        }
        val connectionState = if (mHeadsetProfile == null || mDevice == null) {
            STATE_DISCONNECTED
        } else {
            try {
                mHeadsetProfile!!.getConnectionState(mDevice)
            } catch (e: SecurityException) {
                Timber.e(e, "Permission refusée lors de la récupération de l'état de connexion.")
                STATE_DISCONNECTED
            }
        }
        if (connectionState == STATE_CONNECTED || connectionState == STATE_DISCONNECTED) {
            mIsConnecting = false
        }
        if (!mIsConnecting) {
            return connectionState
        }
        Timber.d("Bug de déconnexion, tentative de mise en connexion")
        return STATE_CONNECTING
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mIsConnecting = true
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            if (getConnectionState() == STATE_DISCONNECTED) {
                Timber.d("Déjà appairé, connexion du casque directe")
                connectHeadset()
                return
            } else {
                mIsConnecting = false
                return
            }
        }
        Timber.d("Appareil non appairé, état=${device.bondState}")
        mIsConnecting = false
    }

    override fun disconnect() {
        disconnectHeadset()
    }

    private fun connectHeadset() {
        setPriority(mDevice, 100)
        mHeadsetProfile ?: return
        if (!context.hasBluetoothConnectPermission()) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée. Connexion du casque impossible.")
            return
        }
        runCatching {
            val method: Method = BluetoothHeadset::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mHeadsetProfile, mDevice)
        }.onFailure { exception ->
            when (exception) {
                is IllegalAccessException -> Timber.e(exception, "Accès illégal!")
                is NoSuchMethodException -> Timber.e(exception, "Méthode connect(BluetoothDevice) introuvable dans le proxy BluetoothHeadset.")
                is InvocationTargetException -> Timber.e(exception, "Impossible d'invoquer connect sur le proxy.")
                else -> Timber.e(exception, "Erreur inconnue lors de connectHeadset")
            }
        }
    }

    private fun disconnectHeadset() {
        setPriority(mDevice, 0)
        mHeadsetProfile ?: return
        if (!context.hasBluetoothConnectPermission()) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée. Déconnexion du casque impossible.")
            return
        }
        runCatching {
            val method: Method = BluetoothHeadset::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mHeadsetProfile, mDevice)
        }.onFailure { exception ->
            when (exception) {
                is IllegalAccessException -> Timber.e(exception, "Accès illégal!")
                is NoSuchMethodException -> Timber.e(exception, "Méthode disconnect(BluetoothDevice) introuvable dans le proxy BluetoothHeadset.")
                is InvocationTargetException -> Timber.e(exception, "Impossible d'invoquer disconnect sur le proxy.")
                else -> Timber.e(exception, "Erreur inconnue lors de disconnectHeadset")
            }
        }
    }

    private fun setPriority(device: BluetoothDevice?, priority: Int) {
        mHeadsetProfile ?: return
        if (!context.hasBluetoothConnectPermission()) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée. Impossible de définir la priorité.")
            return
        }
        runCatching {
            val method: Method = BluetoothHeadset::class.java.getMethod(
                "setPriority",
                BluetoothDevice::class.java,
                Int::class.javaPrimitiveType
            )
            method.invoke(mHeadsetProfile, device, priority)
        }.onFailure {
            Timber.d(it, "Erreur lors de la définition de la priorité")
        }
    }
}

// Extension function pour vérifier la permission BLUETOOTH_CONNECT
private fun Context.hasBluetoothConnectPermission(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) { // Android 12+ (API 31+)
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Avant Android 12, cette permission n'est pas nécessaire
    }
}
