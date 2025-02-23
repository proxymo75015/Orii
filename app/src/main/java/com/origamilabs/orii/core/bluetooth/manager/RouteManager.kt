package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaRouter
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.origamilabs.orii.core.Constants
import com.origamilabs.orii.core.R
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import com.origamilabs.orii.core.bluetooth.connection.A2dpHandler
import com.origamilabs.orii.core.bluetooth.connection.HeadsetHandler
import com.origamilabs.orii.core.bluetooth.connection.ScanManager
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import java.util.*
import kotlin.collections.HashSet

object RouteManager : BaseManager() {

    private const val TAG = "RouteManager"
    private const val TRIGGER_INTERVAL = AbstractSpiCall.DEFAULT_TIMEOUT
    private const val RECONNECTED_INTERVAL = 200

    private var mA2dpProfile: BluetoothProfile? = null
    private var mHeadsetProfile: BluetoothProfile? = null
    private var mMediaRouter: MediaRouter? = null

    private var mHandler: Handler? = null
    private var mHandlerThread: HandlerThread? = null

    // Références aux gestionnaires de profils classiques
    private var mHeadsetHandler: HeadsetHandler? = null
    private var mA2dpHandler: A2dpHandler? = null
    private var mGattHandler: GattHandler? = null

    // La classe d'intent pour la notification (optionnelle)
    private var mIntentClass: Class<*>? = null

    private var mCallTimer: Timer? = null
    private var mCurrentState: Int = 0
    private var mDevice: BluetoothDevice? = null
    private var mDeviceBonded: Boolean = false

    // Liste des callbacks enregistrés
    private val mCallbacks = mutableListOf<Callback?>()

    // Indique si une reconnexion est en cours
    private var isReconnecting = false

    // Runnable déclenché périodiquement pour mettre à jour la route audio
    private val mHandlerRunnable = object : Runnable {
        override fun run() {
            updateAudioRoute()
            mHandler?.postDelayed(this, TRIGGER_INTERVAL.toLong())
        }
    }

    // Service listeners pour les profils Headset et A2DP
    private val mHeadsetServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile?) {
            mHeadsetProfile = bluetoothProfile
        }

        override fun onServiceDisconnected(profile: Int) {
            mHeadsetProfile = null
        }
    }

    private val mA2dpServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile?) {
            mA2dpProfile = bluetoothProfile
        }

        override fun onServiceDisconnected(profile: Int) {
            // Note : Dans le code Java original, on met mHeadsetProfile à null ici – vérifiez si cela est voulu.
            mHeadsetProfile = null
        }
    }

    // Récepteur pour l'état du casque (plug/unplug)
    private val mHeadsetPlugStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // ServerProtocol.DIALOG_PARAM_STATE et AppEventsConstants.EVENT_PARAM_VALUE_NO proviennent des libs Facebook
            if (intent?.extras?.get("state") == "0") {
                audioManager.setBluetoothScoOn(true)
            } else {
                audioManager.setBluetoothScoOn(false)
            }
        }
    }

    // Callback utilisé par ConnectionManager
    private val mBtStateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.bluetooth.adapter.action.STATE_CHANGED" &&
                intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 10
            ) {
                stopForegroundForConnection()
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
        BluetoothHelper.getBluetoothAdapter(mContext).getProfileProxy(mContext, mHeadsetServiceListener, BluetoothProfile.HEADSET)
        BluetoothHelper.getBluetoothAdapter(mContext).getProfileProxy(mContext, mA2dpServiceListener, BluetoothProfile.A2DP)
        mMediaRouter = mContext.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter?
        mHandlerThread = HandlerThread(TAG).apply { start() }
        mHandler = Handler(mHandlerThread!!.looper)
        mContext.registerReceiver(mHeadsetPlugStateReceiver, IntentFilter("android.intent.action.HEADSET_PLUG"))
        (mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager).setBluetoothScoOn(true)
        mContext.registerReceiver(mBtStateChangeReceiver, IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"))
        mContext.registerReceiver(mBondStateReceiver, IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"))
        return true
    }

    // Récepteur pour les changements d'état de liaison du device
    private val mBondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val device = intent?.getParcelableExtra<BluetoothDevice>("android.bluetooth.device.extra.DEVICE")
            if (device == null || !BluetoothHelper.isOriiMacAddressInRange(device.address)) return
            val bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1)
            Log.d(
                TAG,
                String.format(
                    "Bond state changed: %s => %s",
                    intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1),
                    bondState
                )
            )
            Log.d(TAG, "mDevice bind state: ${device.bondState}")
            when (bondState) {
                10, 11 -> mDeviceBonded = false
                12 -> {
                    mDeviceBonded = true
                    connectClassic()
                }
            }
        }
    }

    override fun start() {
        mHandler?.postDelayed(mHandlerRunnable, TRIGGER_INTERVAL.toLong())
    }

    override fun close() {
        mHandler?.removeCallbacksAndMessages(null)
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
                    mHandler?.postDelayed({
                        connectA2dp(device)
                        connectHeadset(device)
                        isReconnecting = false
                    }, RECONNECTED_INTERVAL.toLong())
                }
            } else if (connectedAudioDevices.size == 1 && isPhoneAudioRouteName(currentAudioRouteName)) {
                if (BluetoothHelper.isOriiMacAddressInRange(device.address)) {
                    ConnectionManager.getInstance().disconnectClassic()
                } else {
                    isReconnecting = true
                    disconnectA2dp(device)
                    disconnectHeadset(device)
                    mHandler?.postDelayed({
                        connectA2dp(device)
                        connectHeadset(device)
                        isReconnecting = false
                    }, RECONNECTED_INTERVAL.toLong())
                }
            }
        }
    }

    private fun getConnectedAudioDevices(): Set<BluetoothDevice>? {
        val bondedDevices = getBluetoothAdapter()?.bondedDevices ?: return null
        val result = HashSet<BluetoothDevice>()
        if (getBluetoothAdapter() == null || !getBluetoothAdapter()!!.isEnabled) return null

        if (mHeadsetProfile == null) {
            BluetoothHelper.getBluetoothAdapter(mContext).getProfileProxy(mContext, mHeadsetServiceListener, BluetoothProfile.HEADSET)
            return null
        }
        if (mA2dpProfile == null) {
            BluetoothHelper.getBluetoothAdapter(mContext).getProfileProxy(mContext, mA2dpServiceListener, BluetoothProfile.A2DP)
            return null
        }
        for (device in bondedDevices) {
            if (device.bondState != BluetoothDevice.BOND_BONDED) continue
            val uuids = device.uuids
            if (uuids != null && containsAnyUuid(uuids, Constants.HEADSET_PROFILE_UUIDS) &&
                mHeadsetProfile?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
            ) {
                result.add(device)
            }
            if (uuids != null && containsAnyUuid(uuids, Constants.A2DP_SINK_PROFILE_UUIDS) &&
                mA2dpProfile?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
            ) {
                result.add(device)
            }
        }
        return result
    }

    private fun getCurrentAudioRouteName(): String {
        return mMediaRouter?.selectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO)?.name?.toString() ?: ""
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
            method.invoke(mHeadsetProfile, device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connectHeadset(device: BluetoothDevice) {
        try {
            val method = BluetoothHeadset::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mHeadsetProfile, device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disconnectA2dp(device: BluetoothDevice) {
        try {
            val method = BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mA2dpProfile, device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connectA2dp(device: BluetoothDevice) {
        try {
            val method = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
            method.isAccessible = true
            method.invoke(mA2dpProfile, device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSearch() {
        mHandler?.post {
            close()
            ScanManager.instance.close()
            CommandManager.instance.close()
            RouteManager.stopForegroundForConnection()
        }
    }

    @Synchronized
    fun updateNotificationBatteryLevel(level: Int) {
        updateNotification(
            mContext.getString(R.string.notification_title_connected),
            mContext.getString(R.string.notification_text_battery_level) + (level * 20) + "%"
        )
    }

    private fun updateNotification(title: String, content: String) {
        val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(Constants.ORII_NOTIFICATION_CONNECTION_STATE, setNotification(title, content).build())
    }

    private fun setNotification(title: String, content: String): NotificationCompat.Builder {
        val channelId = mContext.getString(R.string.notification_connection_channel_id)
        val builder = NotificationCompat.Builder(mContext, channelId)
            .setSmallIcon(R.drawable.ic_statusbar)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setShowWhen(false)
        mIntentClass?.let { cls ->
            val intent = Intent(mContext, cls)
            val stackBuilder = TaskStackBuilder.create(mContext)
            stackBuilder.addParentStack(cls)
            stackBuilder.addNextIntent(intent)
            builder.setContentIntent(stackBuilder.getPendingIntent(Constants.ORII_NOTIFICATION_CONNECTION_STATE, 134217728))
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    mContext.getString(R.string.notification_connection_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return builder
    }

    fun stopForegroundForConnection() {
        Log.d(TAG, "stopForegroundForConnection")
        mBluetoothService.stopForeground(true)
    }

    private fun startCheckConnectionTimerTask() {
        mCallTimer?.cancel()
        mCallTimer?.purge()
        mCallTimer = null
        mCallTimer = Timer()
        mCallTimer?.schedule(180000L) {
            if (mCurrentState != 2) {
                stopSearch()
            }
        }
    }

    fun getConnectionState(): Int = mCurrentState

    fun getOriiAddress(): String = mDevice?.address ?: ""

    fun setNotificationIntentClass(cls: Class<*>) {
        mIntentClass = cls
    }

    // Méthode de mise à jour de l'état de la connexion
    @Synchronized
    fun updateConnectionState() {
        val headsetState = mHeadsetHandler?.currentState ?: 0
        val a2dpState = mA2dpHandler?.currentState ?: 0
        var gattState = mGattHandler?.currentState ?: 0
        Log.d(TAG, "updateConnectionState()-headsetState:$headsetState")
        Log.d(TAG, "updateConnectionState()-a2dpState:$a2dpState")
        Log.d(TAG, "updateConnectionState()-gattState:$gattState")

        if (headsetState == BluetoothProfile.STATE_CONNECTED &&
            a2dpState == BluetoothProfile.STATE_CONNECTED && gattState == 0
        ) {
            Log.d(TAG, "updateConnectionState()- device: ${mDevice?.name}")
            Log.d(TAG, "updateConnectionState()- isConnecting(): ${mGattHandler?.isConnecting() ?: false}")
            Log.d(TAG, "updateConnectionState()- isConnectingGatt(): ${mGattHandler?.isConnectingGatt() ?: false}")
            Log.d(TAG, "updateConnectionState()- deviceBondState(): ${mDevice?.bondState}")
            if (mDevice != null && !(mGattHandler?.isConnecting() ?: false)
                && !(mGattHandler?.isConnected() ?: false)
                && !(mGattHandler?.isConnectingGatt() ?: false)
                && !(mGattHandler?.isClosingGatt() ?: false)
            ) {
                mGattHandler?.connect(mDevice)
                gattState = ConnectionHandler.STATE_CONNECTING
            }
        }
        var newState = mCurrentState
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
        Log.d(TAG, "Connection State: $mCurrentState => $newState")
        if (newState != mCurrentState) {
            when (newState) {
                ConnectionHandler.STATE_CONNECTED -> updateNotification(mContext.getString(R.string.notification_title_connected), "")
                ConnectionHandler.STATE_CONNECTING -> {
                    startForegroundForConnection()
                    startCheckConnectionTimerTask()
                }
                ConnectionHandler.STATE_DISCONNECTED -> stopForegroundForConnection()
            }
            mCallbacks.forEach { it?.onOriiStateChange(mCurrentState, newState) }
            mCurrentState = newState
        }
    }
}
