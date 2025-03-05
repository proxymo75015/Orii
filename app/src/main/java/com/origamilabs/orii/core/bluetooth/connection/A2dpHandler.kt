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
import java.lang.reflect.Method
import javax.inject.Inject

/**
 * Handler pour la connexion A2DP.
 *
 * Cette classe gère la connexion/déconnexion d'un périphérique A2DP en utilisant la réflexion
 * pour invoquer les méthodes "connect", "disconnect" et "setPriority" sur l'API BluetoothA2dp.
 */
class A2dpHandler @Inject constructor(
    @ApplicationContext context: Context,
    callback: ConnectionHandler.Callback
) : ConnectionHandler(context, "A2dp State Handler", callback) {

    companion object {
        private const val TAG = "A2dpHandler"
    }

    // Utilisation d'un nom de variable plus court pour éviter la redondance
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
        // Vérification de la permission avant d'obtenir le proxy A2DP
        if (ContextCompat.checkSelfPermission(context, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            BluetoothHelper.getBluetoothAdapter(context)
                ?.getProfileProxy(context, a2dpServiceListener, BluetoothProfile.A2DP)
        } else {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour obtenir le proxy A2DP.")
        }
    }

    override fun close() {
        // Rien à fermer ici
    }

    override fun getConnectionState(): Int {
        // Vérification de la permission pour appeler getConnectionState()
        if (ContextCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
        // Mise à jour de l'état courant si nécessaire
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
        if (a2dpProfile == null) return

        // Pour les versions antérieures, définir la priorité
        if (Build.VERSION.SDK_INT < 26) {
            setPriority(mDevice, 100)
        }

        // Vérification de la permission avant d'invoquer la méthode connect()
        if (ContextCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée.")
            return
        }

        try {
            val connectMethod: Method = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
            connectMethod.isAccessible = true
            connectMethod.invoke(a2dpProfile, mDevice)
        } catch (e: SecurityException) {
            Timber.e(e, "Erreur de sécurité lors de la connexion A2DP")
        } catch (e: IllegalAccessException) {
            Timber.e(e, "Accès illégal!")
        } catch (e: NoSuchMethodException) {
            Timber.e(e, "Méthode connect(BluetoothDevice) introuvable dans le proxy BluetoothA2dp.")
        } catch (e: InvocationTargetException) {
            Timber.e(e, "Impossible d'invoquer la méthode connect(BluetoothDevice) sur le proxy.")
        }
    }

    private fun disconnectA2dp() {
        if (a2dpProfile == null) return

        if (Build.VERSION.SDK_INT < 26) {
            setPriority(mDevice, 0)
        }

        if (ContextCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée.")
            return
        }

        try {
            val disconnectMethod: Method = BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            disconnectMethod.isAccessible = true
            disconnectMethod.invoke(a2dpProfile, mDevice)
        } catch (e: SecurityException) {
            Timber.e(e, "Erreur de sécurité lors de la déconnexion A2DP")
        } catch (e: IllegalAccessException) {
            Timber.e(e, "Accès illégal!")
        } catch (e: NoSuchMethodException) {
            Timber.e(e, "Méthode disconnect(BluetoothDevice) introuvable dans le proxy BluetoothA2dp.")
        } catch (e: InvocationTargetException) {
            Timber.e(e, "Impossible d'invoquer la méthode disconnect(BluetoothDevice) sur le proxy.")
        }
    }

    private fun setPriority(device: BluetoothDevice?, priority: Int) {
        if (a2dpProfile == null) return

        if (ContextCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée pour définir la priorité.")
            return
        }

        try {
            val setPriorityMethod: Method = BluetoothA2dp::class.java.getMethod(
                "setPriority", BluetoothDevice::class.java, Int::class.javaPrimitiveType)
            setPriorityMethod.invoke(a2dpProfile, device, priority)
        } catch (e: SecurityException) {
            Timber.e(e, "Erreur de sécurité lors de la définition de la priorité")
        } catch (e: Exception) {
            Timber.d(e, "Erreur lors de la définition de la priorité")
        }
    }
}
