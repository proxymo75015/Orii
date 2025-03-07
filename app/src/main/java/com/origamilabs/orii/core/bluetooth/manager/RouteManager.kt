package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaRouter
import android.os.Build
import android.os.ParcelUuid
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import com.origamilabs.orii.core.Constants
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IManager {

    companion object {
        private const val TAG = "RouteManager"
        private const val TRIGGER_INTERVAL: Long = 10_000L   // 10 secondes
        private const val RECONNECTED_INTERVAL: Long = 200L
        private const val CONNECTION_TIMEOUT: Long = 180_000L  // 3 minutes
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var updateJob: Job? = null
    private var checkConnectionJob: Job? = null

    private var a2dpProfile: BluetoothProfile? = null
    private var headsetProfile: BluetoothProfile? = null
    private var mediaRouter: MediaRouter? =
        context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as? MediaRouter

    private var device: BluetoothDevice? = null
    private var deviceBonded: Boolean = false

    private val callbacks = mutableListOf<Callback?>()

    @Volatile
    private var isReconnecting = false

    interface Callback {
        fun onA2dpStateChange(oldState: Int, newState: Int)
        fun onGattStateChange(oldState: Int, newState: Int)
        fun onHeadsetStateChange(oldState: Int, newState: Int)
        fun onOriiRemoveBond()
        fun onOriiStateChange(oldState: Int, newState: Int)
    }

    // Récepteurs centralisés pour gérer les événements Bluetooth.
    private val headsetPlugReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val state = intent?.getIntExtra("state", -1) ?: -1
            (ctx?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.apply {
                isBluetoothScoOn = (state == 0)
            }
        }
    }

    private val btStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED &&
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                stopForegroundForConnection()
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra("android.bluetooth.device.extra.DEVICE", BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra("android.bluetooth.device.extra.DEVICE")
            }
            if (device == null || !BluetoothHelper.isOriiMacAddressInRange(device.address)) return
            val bondState = intent?.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1) ?: -1
            Timber.d("Changement d'état de liaison : $bondState")
            deviceBonded = (bondState == BluetoothDevice.BOND_BONDED)
            if (deviceBonded) connectClassic()
        }
    }

    override fun initialize(): Boolean {
        val bluetoothAdapter = BluetoothHelper.getBluetoothAdapter(context)
        if (bluetoothAdapter == null) {
            Timber.e("Adaptateur Bluetooth non disponible")
            return false
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée")
            return false
        }
        bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                headsetProfile = proxy
            }
            override fun onServiceDisconnected(profile: Int) {
                headsetProfile = null
            }
        }, BluetoothProfile.HEADSET)
        bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                a2dpProfile = proxy
            }
            override fun onServiceDisconnected(profile: Int) {
                a2dpProfile = null
            }
        }, BluetoothProfile.A2DP)
        context.registerReceiver(headsetPlugReceiver, IntentFilter("android.intent.action.HEADSET_PLUG"))
        (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.isBluetoothScoOn = true
        context.registerReceiver(btStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context.registerReceiver(bondStateReceiver, IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"))
        return true
    }

    override fun start() {
        updateJob = scope.launch {
            while (isActive) {
                updateAudioRoute()
                delay(TRIGGER_INTERVAL)
            }
        }
    }

    override fun close() {
        updateJob?.cancel()
        checkConnectionJob?.cancel()
        scope.cancel()
        try {
            context.unregisterReceiver(headsetPlugReceiver)
            context.unregisterReceiver(btStateReceiver)
            context.unregisterReceiver(bondStateReceiver)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la désinscription des récepteurs")
        }
    }

    @Synchronized
    fun updateAudioRoute() {
        val connectedAudioDevices = getConnectedAudioDevices() ?: return
        val currentAudioRouteName = getCurrentAudioRouteName()
        Timber.d("Mise à jour de la route audio : ${connectedAudioDevices.size} appareils, route actuelle : $currentAudioRouteName")
        if (isReconnecting) return

        for (device in connectedAudioDevices) {
            if (connectedAudioDevices.size >= 2 && (isOriiAudioRouteName(currentAudioRouteName) || isPhoneAudioRouteName(currentAudioRouteName))) {
                if (!BluetoothHelper.isOriiMacAddressInRange(device.address)) {
                    isReconnecting = true
                    disconnectA2dp(device)
                    disconnectHeadset(device)
                    scope.launch {
                        delay(RECONNECTED_INTERVAL)
                        connectA2dp(device)
                        connectHeadset(device)
                        isReconnecting = false
                    }
                }
            } else if (connectedAudioDevices.size == 1 && isPhoneAudioRouteName(currentAudioRouteName)) {
                if (BluetoothHelper.isOriiMacAddressInRange(device.address)) {
                    disconnectClassic()
                } else {
                    isReconnecting = true
                    disconnectA2dp(device)
                    disconnectHeadset(device)
                    scope.launch {
                        delay(RECONNECTED_INTERVAL)
                        connectA2dp(device)
                        connectHeadset(device)
                        isReconnecting = false
                    }
                }
            }
        }
    }

    private fun getConnectedAudioDevices(): Set<BluetoothDevice>? {
        val bluetoothAdapter = BluetoothHelper.getBluetoothAdapter(context) ?: return null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Permission BLUETOOTH_CONNECT non accordée")
            return null
        }
        val bondedDevices = bluetoothAdapter.bondedDevices ?: return null
        val result = mutableSetOf<BluetoothDevice>()
        if (!bluetoothAdapter.isEnabled) return null

        if (headsetProfile == null || a2dpProfile == null) {
            bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                    if (profile == BluetoothProfile.HEADSET) headsetProfile = proxy
                    if (profile == BluetoothProfile.A2DP) a2dpProfile = proxy
                }
                override fun onServiceDisconnected(profile: Int) {
                    if (profile == BluetoothProfile.HEADSET) headsetProfile = null
                    if (profile == BluetoothProfile.A2DP) a2dpProfile = null
                }
            }, BluetoothProfile.HEADSET)
            return null
        }
        for (device in bondedDevices) {
            if (device.bondState != BluetoothDevice.BOND_BONDED) continue
            val uuids = device.uuids
            if (uuids != null && containsAnyUuid(uuids, Constants.HEADSET_PROFILE_UUIDS) &&
                headsetProfile?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                result.add(device)
            }
            if (uuids != null && containsAnyUuid(uuids, Constants.A2DP_SINK_PROFILE_UUIDS) &&
                a2dpProfile?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                result.add(device)
            }
        }
        return result
    }

    private fun getCurrentAudioRouteName(): String {
        return mediaRouter?.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO)?.name?.toString() ?: ""
    }

    private fun isOriiAudioRouteName(routeName: String): Boolean {
        return routeName.equals("ORII", ignoreCase = true) || routeName.equals("ORII_BLE", ignoreCase = true)
    }

    private fun isPhoneAudioRouteName(routeName: String): Boolean {
        return routeName.equals("Phone", ignoreCase = true) || routeName.equals("Téléphone", ignoreCase = true)
    }

    private fun containsAnyUuid(uuids: Array<ParcelUuid>, targets: Array<ParcelUuid>): Boolean {
        val uuidSet = uuids.toSet()
        return targets.any { it in uuidSet }
    }

    private fun disconnectHeadset(device: BluetoothDevice) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Timber.e("Permission BLUETOOTH_CONNECT non accordée pour déconnecter le casque")
                return
            }
            val method = BluetoothHeadset::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(headsetProfile, device)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la déconnexion du casque")
        }
    }

    private fun connectHeadset(device: BluetoothDevice) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Timber.e("Permission BLUETOOTH_CONNECT non accordée pour connecter le casque")
                return
            }
            val method = BluetoothHeadset::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(headsetProfile, device)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la connexion du casque")
        }
    }

    private fun disconnectA2dp(device: BluetoothDevice) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Timber.e("Permission BLUETOOTH_CONNECT non accordée pour déconnecter A2DP")
                return
            }
            val method = BluetoothAdapter::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(BluetoothHelper.getBluetoothAdapter(context), device)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la déconnexion d'A2DP")
        }
    }

    private fun connectA2dp(device: BluetoothDevice) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Timber.e("Permission BLUETOOTH_CONNECT non accordée pour connecter A2DP")
                return
            }
            val method = BluetoothAdapter::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(BluetoothHelper.getBluetoothAdapter(context), device)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la connexion d'A2DP")
        }
    }

    // Méthodes pour la connexion/déconnexion classique (à implémenter si nécessaire)
    private fun disconnectClassic() {
        Timber.d("Déconnexion classique effectuée")
    }

    private fun connectClassic() {
        Timber.d("Connexion classique effectuée")
    }

    private fun stopForegroundForConnection() {
        Timber.d("Arrêt du service au premier plan pour la connexion")
    }
}
