package com.origamilabs.orii.core.bluetooth.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothA2dp
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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.origamilabs.orii.core.Constants
import com.origamilabs.orii.core.R
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import com.origamilabs.orii.core.bluetooth.connection.A2dpHandler
import com.origamilabs.orii.core.bluetooth.connection.HeadsetHandler
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteManager @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseManager() {

    companion object {
        private const val TAG = "RouteManager"
        private const val TRIGGER_INTERVAL: Long = 10_000L // 10 secondes
        private const val RECONNECTED_INTERVAL: Long = 200L
        private const val CONNECTION_TIMEOUT: Long = 180_000L // 3 minutes
    }

    // Scope dédié pour les coroutines du manager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Jobs pour la mise à jour périodique et la vérification de connexion
    private var updateJob: Job? = null
    private var checkConnectionJob: Job? = null

    private var a2dpProfile: BluetoothProfile? = null
    private var headsetProfile: BluetoothProfile? = null
    private var mediaRouter: MediaRouter? =
        context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as? MediaRouter

    // Références aux gestionnaires de profils (à injecter ou initialiser selon votre architecture)
    private var headsetHandler: HeadsetHandler? = null
    private var a2dpHandler: A2dpHandler? = null
    private var gattHandler: GattHandler? = null

    // Classe d'intent pour la notification (optionnelle)
    private var intentClass: Class<*>? = null

    private var currentState: Int = 0
    private var device: BluetoothDevice? = null
    private var deviceBonded: Boolean = false

    // Liste des callbacks enregistrés
    private val callbacks = mutableListOf<Callback?>()

    // Indique si une reconnexion est en cours
    @Volatile
    private var isReconnecting = false

    // Service listeners pour les profils Headset et A2DP
    private val headsetServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile?) {
            headsetProfile = bluetoothProfile
        }
        override fun onServiceDisconnected(profile: Int) {
            headsetProfile = null
        }
    }
    private val a2dpServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile?) {
            a2dpProfile = bluetoothProfile
        }
        override fun onServiceDisconnected(profile: Int) {
            // Dans le code original, on met headsetProfile à null ici – vérifiez si c'est voulu.
            headsetProfile = null
        }
    }

    // Récepteur pour l'état du casque (plug/unplug)
    private val headsetPlugStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val audioManager = ctx?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (intent?.extras?.get("state") == "0") {
                audioManager.isBluetoothScoOn = true
            } else {
                audioManager.isBluetoothScoOn = false
            }
        }
    }

    // Récepteur pour les changements d'état de l'adaptateur Bluetooth
    private val btStateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED &&
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF
            ) {
                stopForegroundForConnection()
            }
        }
    }

    // Récepteur pour les changements d'état de liaison d'un appareil
    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val device = intent?.getParcelableExtra<BluetoothDevice>("android.bluetooth.device.extra.DEVICE")
            if (device == null || !BluetoothHelper.isOriiMacAddressInRange(device.address)) return
            val bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1)
            Log.d(TAG, "Bond state changed: ${intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1)} => $bondState")
            Log.d(TAG, "Device bond state: ${device.bondState}")
            when (bondState) {
                BluetoothDevice.BOND_NONE, BluetoothDevice.BOND_BONDING -> deviceBonded = false
                BluetoothDevice.BOND_BONDED -> {
                    deviceBonded = true
                    connectClassic()
                }
            }
        }
    }

    interface Callback {
        fun onA2dpStateChange(oldState: Int, newState: Int)
        fun onGattStateChange(oldState: Int, newState: Int)
        fun onHeadsetStateChange(oldState: Int, newState: Int)
        fun onOriiRemoveBond()
        fun onOriiStateChange(oldState: Int, newState: Int)
    }

    override fun onInitialize(): Boolean {
        val bluetoothAdapter = BluetoothHelper.getBluetoothAdapter(context)
        bluetoothAdapter?.getProfileProxy(context, headsetServiceListener, BluetoothProfile.HEADSET)
        bluetoothAdapter?.getProfileProxy(context, a2dpServiceListener, BluetoothProfile.A2DP)
        context.registerReceiver(headsetPlugStateReceiver, IntentFilter("android.intent.action.HEADSET_PLUG"))
        (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.isBluetoothScoOn = true
        context.registerReceiver(btStateChangeReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context.registerReceiver(bondStateReceiver, IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"))
        return true
    }

    override fun start() {
        // Lancement d'une coroutine pour mettre à jour périodiquement la route audio.
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
    }

    @Synchronized
    fun updateAudioRoute() {
        val connectedAudioDevices = getConnectedAudioDevices() ?: return
        val currentAudioRouteName = getCurrentAudioRouteName()
        Log.d(TAG, "updateAudioRoute()-connected audio devices size: ${connectedAudioDevices.size}, currentAudioRouteName is: $currentAudioRouteName")
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
                    ConnectionManager.getInstance().disconnectClassic()
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
        val bondedDevices = bluetoothAdapter.bondedDevices ?: return null
        val result = mutableSetOf<BluetoothDevice>()
        if (!bluetoothAdapter.isEnabled) return null

        if (headsetProfile == null) {
            bluetoothAdapter.getProfileProxy(context, headsetServiceListener, BluetoothProfile.HEADSET)
            return null
        }
        if (a2dpProfile == null) {
            bluetoothAdapter.getProfileProxy(context, a2dpServiceListener, BluetoothProfile.A2DP)
            return null
        }
        for (device in bondedDevices) {
            if (device.bondState != BluetoothDevice.BOND_BONDED) continue
            val uuids = device.uuids
            if (uuids != null && containsAnyUuid(uuids, Constants.HEADSET_PROFILE_UUIDS) &&
                headsetProfile?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
            ) {
                result.add(device)
            }
            if (uuids != null && containsAnyUuid(uuids, Constants.A2DP_SINK_PROFILE_UUIDS) &&
                a2dpProfile?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
            ) {
                result.add(device)
            }
        }
        return result
    }

    private fun getCurrentAudioRouteName(): String {
        return mediaRouter?.selectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO)?.name?.toString() ?: ""
    }

    private fun isOriiAudioRouteName(routeName: String): Boolean {
        return routeName.equals("ORII", ignoreCase = true) || routeName.equals("ORII_BLE", ignoreCase = true)
    }

    private fun isPhoneAudioRouteName(routeName: String): Boolean {
        return routeName.equals("Phone", ignoreCase = true) || routeName.equals("手機", ignoreCase = true)
    }

    private fun containsAnyUuid(uuids: Array<ParcelUuid>, targets: Array<ParcelUuid>): Boolean {
        val uuidSet = uuids.toSet()
        return targets.any { it in uuidSet }
    }

    private fun disconnectHeadset(device: BluetoothDevice) {
        try {
            val method = BluetoothHeadset::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(headsetProfile, device)
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting headset", e)
        }
    }

    fun connectHeadset(device: BluetoothDevice) {
        try {
            val method = BluetoothHeadset::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(headsetProfile, device)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting headset", e)
        }
    }

    private fun disconnectA2dp(device: BluetoothDevice) {
        try {
            val method = BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(a2dpProfile, device)
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting A2DP", e)
        }
    }

    fun connectA2dp(device: BluetoothDevice) {
        try {
            val method = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(a2dpProfile, device)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting A2DP", e)
        }
    }

    fun stopSearch() {
        scope.launch {
            close()
            ScanManager.instance.close()
            CommandManager.instance.close()
            stopForegroundForConnection()
        }
    }

    @Synchronized
    fun updateNotificationBatteryLevel(level: Int) {
        updateNotification(
            context.getString(R.string.notification_title_connected),
            context.getString(R.string.notification_text_battery_level) + (level * 20) + "%"
        )
    }

    private fun updateNotification(title: String, content: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(Constants.ORII_NOTIFICATION_CONNECTION_STATE, setNotification(title, content).build())
    }

    private fun setNotification(title: String, content: String): NotificationCompat.Builder {
        val channelId = context.getString(R.string.notification_connection_channel_id)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_statusbar)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setShowWhen(false)
        intentClass?.let { cls ->
            val intent = Intent(context, cls)
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(cls)
            stackBuilder.addNextIntent(intent)
            builder.setContentIntent(
                stackBuilder.getPendingIntent(
                    Constants.ORII_NOTIFICATION_CONNECTION_STATE,
                    NotificationCompat.FLAG_UPDATE_CURRENT
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    context.getString(R.string.notification_connection_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return builder
    }

    fun stopForegroundForConnection() {
        Log.d(TAG, "stopForegroundForConnection")
        // Supposons que bluetoothService est fourni par BaseManager ou via injection.
        bluetoothService?.stopForeground(true)
    }

    private fun startCheckConnectionTimerTask() {
        checkConnectionJob?.cancel()
        checkConnectionJob = scope.launch {
            delay(CONNECTION_TIMEOUT)
            if (currentState != ConnectionHandler.STATE_CONNECTED) {
                stopSearch()
            }
        }
    }

    fun getConnectionState(): Int = currentState

    fun getOriiAddress(): String = device?.address ?: ""

    fun setNotificationIntentClass(cls: Class<*>) {
        intentClass = cls
    }

    @Synchronized
    fun updateConnectionState() {
        val headsetState = headsetHandler?.currentState ?: 0
        val a2dpState = a2dpHandler?.currentState ?: 0
        var gattState = gattHandler?.currentState ?: 0
        Log.d(TAG, "updateConnectionState()-headsetState: $headsetState")
        Log.d(TAG, "updateConnectionState()-a2dpState: $a2dpState")
        Log.d(TAG, "updateConnectionState()-gattState: $gattState")

        if (headsetState == BluetoothProfile.STATE_CONNECTED &&
            a2dpState == BluetoothProfile.STATE_CONNECTED && gattState == 0
        ) {
            Log.d(TAG, "updateConnectionState()- device: ${device?.name}")
            Log.d(TAG, "updateConnectionState()- isConnecting(): ${gattHandler?.isConnecting() ?: false}")
            Log.d(TAG, "updateConnectionState()- isConnectingGatt(): ${gattHandler?.isConnectingGatt() ?: false}")
            Log.d(TAG, "updateConnectionState()- deviceBondState(): ${device?.bondState}")
            if (device != null &&
                !(gattHandler?.isConnecting() ?: false) &&
                !(gattHandler?.isConnected() ?: false) &&
                !(gattHandler?.isConnectingGatt() ?: false) &&
                !(gattHandler?.isClosingGatt() ?: false)
            ) {
                gattHandler?.connect(device)
                gattState = ConnectionHandler.STATE_CONNECTING
            }
        }
        var newState = currentState
        if (a2dpState == 0 || headsetState == 0) {
            connectClassic()
        }
        newState = if (gattState == BluetoothProfile.STATE_CONNECTED &&
            a2dpState == BluetoothProfile.STATE_CONNECTED &&
            headsetState == BluetoothProfile.STATE_CONNECTED
        ) {
            ConnectionHandler.STATE_CONNECTED
        } else {
            if (gattState != ConnectionHandler.STATE_CONNECTING &&
                a2dpState != ConnectionHandler.STATE_CONNECTING &&
                headsetState != ConnectionHandler.STATE_CONNECTING
            ) {
                if (gattState != 0 && a2dpState != 0 && headsetState != 0) newState else 0
            } else 1
        }
        Log.d(TAG, "Connection State: $currentState => $newState")
        if (newState != currentState) {
            when (newState) {
                ConnectionHandler.STATE_CONNECTED ->
                    updateNotification(context.getString(R.string.notification_title_connected), "")
                ConnectionHandler.STATE_CONNECTING -> {
                    startForegroundForConnection()
                    startCheckConnectionTimerTask()
                }
                ConnectionHandler.STATE_DISCONNECTED -> stopForegroundForConnection()
            }
            callbacks.forEach { it?.onOriiStateChange(currentState, newState) }
            currentState = newState
        }
    }

    // Méthode à implémenter pour la connexion Bluetooth classique.
    private fun connectClassic() {
        // Implémentez ici la logique de connexion en Bluetooth classique.
    }
}
