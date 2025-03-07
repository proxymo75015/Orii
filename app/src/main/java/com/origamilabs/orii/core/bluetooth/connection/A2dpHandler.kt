package com.origamilabs.orii.core.bluetooth.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Gère la connexion A2DP à un périphérique Bluetooth.
 */
class A2dpHandler @Inject constructor(
    @ApplicationContext context: Context,
    callback: Callback,
    private val permissionDelegate: PermissionRequestDelegate? = null
) : ConnectionHandler(context, "A2dp State Handler", callback) {

    companion object {
        private const val TAG = "A2dpHandler"
    }

    private var a2dpProfile: BluetoothProfile? = null

    private val a2dpServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
            Timber.d("Connecté au profil $profile")
            a2dpProfile = bluetoothProfile
        }
        override fun onServiceDisconnected(profile: Int) {
            a2dpProfile = null
        }
    }

    init {
        if (mContext.hasBluetoothConnectPermission()) {
            BluetoothHelper.getBluetoothAdapter(mContext)
                ?.getProfileProxy(mContext, a2dpServiceListener, BluetoothProfile.A2DP)
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour obtenir le proxy A2DP.")
            permissionDelegate?.requestBluetoothPermission(BluetoothHelper.REQUEST_CODE_BLUETOOTH_CONNECT)
                ?: Timber.e("Aucun delegate pour la demande de permission.")
        }
    }

    override fun close() {
        a2dpProfile?.let {
            BluetoothHelper.getBluetoothAdapter(mContext)?.closeProfileProxy(BluetoothProfile.A2DP, it)
        }
    }

    override fun getConnectionState(): Int {
        if (!mContext.hasBluetoothConnectPermission()) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée lors de la récupération de l'état.")
            return BluetoothProfile.STATE_DISCONNECTED
        }
        val connectionState = try {
            if (a2dpProfile == null || mDevice == null) {
                BluetoothProfile.STATE_DISCONNECTED
            } else {
                a2dpProfile!!.getConnectionState(mDevice)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Erreur de sécurité lors de la récupération de l'état.")
            BluetoothProfile.STATE_DISCONNECTED
        }
        if (connectionState == BluetoothProfile.STATE_CONNECTED || connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mCurrentState = connectionState
        }
        // Gestion d'un petit bug de déconnexion
        if (mCurrentState == STATE_CONNECTING) {
            Timber.d("Bug de déconnexion, tentative de mise en connexion")
            return BluetoothProfile.STATE_CONNECTING
        }
        return connectionState
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mCurrentState = STATE_CONNECTING
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            if (getConnectionState() == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.d("Déjà appairé, connexion A2DP directe")
                connectA2dp()
                return
            } else {
                mCurrentState = BluetoothProfile.STATE_DISCONNECTED
                return
            }
        }
        Timber.d("Appareil non appairé, état=${device.bondState}")
        mCurrentState = BluetoothProfile.STATE_DISCONNECTED
    }

    override fun disconnect() {
        disconnectA2dp()
    }

    private fun connectA2dp() {
        a2dpProfile?.let { profile ->
            if (!mContext.hasBluetoothConnectPermission()) {
                Timber.e("Permission manquante : demande via le delegate")
                permissionDelegate?.requestBluetoothPermission(BluetoothHelper.REQUEST_CODE_BLUETOOTH_CONNECT)
                return
            }
            runCatching {
                // Pour Android < O, on peut ajuster la priorité
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setPriority(mDevice, 100)
                }
                val connectMethod = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
                connectMethod.isAccessible = true
                connectMethod.invoke(profile, mDevice)
            }.onFailure { exception ->
                when (exception) {
                    is SecurityException -> {
                        Timber.e("Permission refusée : demande explicite")
                        permissionDelegate?.requestBluetoothPermission(BluetoothHelper.REQUEST_CODE_BLUETOOTH_CONNECT)
                    }
                    // ... autres cas d'erreur si nécessaire
                }
            }
        }
    }

    private fun disconnectA2dp() {
        a2dpProfile?.let { profile ->
            if (!mContext.hasBluetoothConnectPermission()) {
                Timber.e("Permission manquante : demande via le delegate")
                permissionDelegate?.requestBluetoothPermission(BluetoothHelper.REQUEST_CODE_BLUETOOTH_CONNECT)
                return
            }
            runCatching {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setPriority(mDevice, 0)
                }
                val disconnectMethod = BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
                disconnectMethod.isAccessible = true
                disconnectMethod.invoke(profile, mDevice)
            }.onFailure { exception ->
                when (exception) {
                    is SecurityException -> {
                        Timber.e("Permission refusée : demande explicite")
                        permissionDelegate?.requestBluetoothPermission(BluetoothHelper.REQUEST_CODE_BLUETOOTH_CONNECT)
                    }
                    // ... autres cas d'erreur si nécessaire
                }
            }
        }
    }

    private fun setPriority(device: BluetoothDevice?, priority: Int) {
        a2dpProfile ?: return
        if (!mContext.hasBluetoothConnectPermission()) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour définir la priorité.")
            return
        }
        runCatching {
            val setPriorityMethod = BluetoothA2dp::class.java.getMethod(
                "setPriority", BluetoothDevice::class.java, Int::class.javaPrimitiveType
            )
            setPriorityMethod.invoke(a2dpProfile, device, priority)
        }.onFailure {
            Timber.d(it, "Erreur lors de la définition de la priorité")
        }
    }
}

/**
 * Vérifie si la permission BLUETOOTH_CONNECT est accordée.
 * Sur les versions d'Android antérieures à 12 (API < 31), cette permission n'existe pas.
 */
@SuppressLint("InlinedApi") // Pour éviter l’avertissement sur BLUETOOTH_CONNECT
private fun Context.hasBluetoothConnectPermission(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        // Sur les anciennes versions, pas besoin de vérifier : la permission n'existe pas
        true
    } else {
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
    }
}
