package com.origamilabs.orii.core.bluetooth.connection

import android.Manifest.permission.BLUETOOTH_CONNECT
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
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject

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
            permissionDelegate?.requestBluetoothPermission()
                ?: Timber.e("Aucun delegate pour la demande de permission.")
        }
    }

    override fun close() {
        // Rien à fermer ici
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
                if (mContext.hasBluetoothConnectPermission()) {
                    a2dpProfile!!.getConnectionState(mDevice)
                } else {
                    Timber.e("Permission BLUETOOTH_CONNECT refusée pour getConnectionState(mDevice)")
                    BluetoothProfile.STATE_DISCONNECTED
                }

            }
        } catch (e: SecurityException) {
            Timber.e(e, "Erreur de sécurité lors de la récupération de l'état.")
            BluetoothProfile.STATE_DISCONNECTED
        }
        if (connectionState == BluetoothProfile.STATE_CONNECTED || connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mCurrentState = connectionState
        }
        if (mCurrentState == STATE_CONNECTING) {
            Timber.d("Bug de déconnexion, tentative de mise en connexion")
            return BluetoothProfile.STATE_CONNECTING
        }
        return connectionState
    }

    override fun connect(device: BluetoothDevice) {
        setDevice(device)
        mCurrentState = STATE_CONNECTING

        val hasPermission = mContext.hasBluetoothConnectPermission()

        val bondState = if (hasPermission) {
            try {
                device.bondState
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException lors de l'accès à bondState (ligne 92)")
                permissionDelegate?.requestBluetoothPermission()
                BluetoothDevice.BOND_NONE
            }
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT refusée lors de l'accès à bondState")
            permissionDelegate?.requestBluetoothPermission()
            BluetoothDevice.BOND_NONE
        }

        if (bondState == BluetoothDevice.BOND_BONDED) {
            if (getConnectionState() == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.d("Déjà appairé, connexion A2DP directe")
                connectA2dp()
                return
            } else {
                mCurrentState = BluetoothProfile.STATE_DISCONNECTED
                return
            }
        }

        if (hasPermission) {
            val bondStateLog = if (mContext.hasBluetoothConnectPermission()) {
                try {
                    device.bondState.toString()
                } catch (e: SecurityException) {
                    Timber.e(e, "SecurityException lors de l'accès à bondState pour log (ligne 117)")
                    permissionDelegate?.requestBluetoothPermission()
                    "Erreur accès bondState"
                }
            } else {
                Timber.e("Permission BLUETOOTH_CONNECT refusée lors du log bondState (ligne 117)")
                permissionDelegate?.requestBluetoothPermission()
                "Permission manquante"
            }

            Timber.d("Appareil non appairé, état=$bondStateLog")

        } else {
            Timber.e("Permission BLUETOOTH_CONNECT refusée lors du log bondState")
            permissionDelegate?.requestBluetoothPermission()
        }

        mCurrentState = BluetoothProfile.STATE_DISCONNECTED
    }

    override fun disconnect() {
        disconnectA2dp()
    }

    private fun connectA2dp() {
        a2dpProfile?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                if (mContext.hasBluetoothConnectPermission()) {
                    try {
                        setPriority(mDevice, 100)
                    } catch (e: SecurityException) {
                        Timber.e(e, "SecurityException lors de setPriority (ligne 111)")
                        permissionDelegate?.requestBluetoothPermission()
                    }
                } else {
                    Timber.e("Permission BLUETOOTH_CONNECT refusée lors de l'appel à setPriority()")
                    permissionDelegate?.requestBluetoothPermission()
                }
            }

            if (!mContext.hasBluetoothConnectPermission()) {
                Timber.e("Permission BLUETOOTH_CONNECT non accordée pour l'appel connect(), demande de permission en cours...")
                permissionDelegate?.requestBluetoothPermission()
                return
            }

            runCatching {
                val connectMethod = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
                connectMethod.isAccessible = true
                connectMethod.invoke(it, mDevice)
            }.onFailure { exception ->
                when (exception) {
                    is IllegalAccessException -> Timber.e(exception, "Accès illégal!")
                    is NoSuchMethodException -> Timber.e(exception, "Méthode connect(BluetoothDevice) introuvable.")
                    is InvocationTargetException -> Timber.e(exception, "Impossible d'invoquer connect.")
                    is SecurityException -> Timber.e(exception, "Permission BLUETOOTH_CONNECT refusée.")
                    else -> Timber.e(exception, "Erreur inconnue lors de connectA2dp")
                }
            }
        }
    }

    private fun disconnectA2dp() {
        a2dpProfile?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                if (mContext.hasBluetoothConnectPermission()) {
                    setPriority(mDevice, 0)
                } else {
                    Timber.e("Permission BLUETOOTH_CONNECT refusée lors de l'appel à setPriority()")
                    permissionDelegate?.requestBluetoothPermission()
                }
            }

            if (!mContext.hasBluetoothConnectPermission()) {
                Timber.e("Permission BLUETOOTH_CONNECT non accordée pour l'appel disconnect(), demande de permission en cours...")
                permissionDelegate?.requestBluetoothPermission()
                return
            }

            runCatching {
                val disconnectMethod = BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
                disconnectMethod.isAccessible = true
                disconnectMethod.invoke(it, mDevice)
            }.onFailure { exception ->
                when (exception) {
                    is IllegalAccessException -> Timber.e(exception, "Accès illégal!")
                    is NoSuchMethodException -> Timber.e(exception, "Méthode disconnect(BluetoothDevice) introuvable.")
                    is InvocationTargetException -> Timber.e(exception, "Impossible d'invoquer disconnect.")
                    is SecurityException -> Timber.e(exception, "Permission BLUETOOTH_CONNECT refusée.")
                    else -> Timber.e(exception, "Erreur inconnue lors de disconnectA2dp")
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

// Extension function pour vérifier la permission BLUETOOTH_CONNECT
private fun Context.hasBluetoothConnectPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
        ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        true // La permission n'est pas nécessaire avant Android 12
    }
}
