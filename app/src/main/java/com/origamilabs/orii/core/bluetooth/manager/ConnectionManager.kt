package com.origamilabs.orii.core.bluetooth.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.origamilabs.orii.core.Constants
import com.origamilabs.orii.core.R
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import com.origamilabs.orii.core.bluetooth.connection.A2dpHandler
import com.origamilabs.orii.core.bluetooth.connection.ConnectionHandler
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import com.origamilabs.orii.core.bluetooth.connection.HeadsetHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    // Ces dépendances doivent être fournies par Hilt (voir vos modules)
    private val bluetoothService: BluetoothService,
    private val routeManager: RouteManager
) : BaseManager() {

    companion object {
        private const val TAG = "ConnectionManager"
    }

    // Dépendances internes
    private var mA2dpHandler: A2dpHandler? = null
    private var mGattHandler: GattHandler? = null
    private var mHeadsetHandler: HeadsetHandler? = null
    private var mIntentClass: Class<*>? = null
    private var mDevice: BluetoothDevice? = null
    private var mDeviceBonded: Boolean = false
    private var mCurrentState: Int = 0

    // Liste des callbacks enregistrés
    private val mCallbacks: MutableList<Callback?> = ArrayList()

    // Scope pour les coroutines
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Job de vérification de connexion (remplace Timer)
    private var checkConnectionJob: Job? = null

    // Receivers
    private val mBondStateReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val bluetoothDevice = intent?.getParcelableExtra<BluetoothDevice>("android.bluetooth.device.extra.DEVICE")
            if (bluetoothDevice == null || !BluetoothHelper.isOriiMacAddressInRange(bluetoothDevice.address)) return
            val bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1)
            Log.d(TAG, "Bond state changed: ${intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1)} => $bondState")
            Log.d(TAG, "mDevice bond state: ${bluetoothDevice.bondState}")
            when (bondState) {
                10, 11 -> mDeviceBonded = false
                12 -> {
                    mDeviceBonded = true
                    connectClassic()
                }
            }
        }
    }

    private val mBtStateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.bluetooth.adapter.action.STATE_CHANGED" &&
                intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 10
            ) {
                stopForegroundForConnection()
            }
        }
    }

    // Implémentations des callbacks avec le bon nom de méthode
    private val mHeadsetCallback = object : ConnectionHandler.Callback {
        override fun onStateChanged(oldState: Int, newState: Int) {
            updateConnectionState()
            if (oldState != newState) {
                mCallbacks.forEach { it?.onHeadsetStateChange(oldState, newState) }
            }
        }
    }

    private val mA2dpHandlerCallback = object : ConnectionHandler.Callback {
        override fun onStateChanged(oldState: Int, newState: Int) {
            updateConnectionState()
            if (oldState != newState) {
                mCallbacks.forEach { it?.onA2dpStateChange(oldState, newState) }
            }
        }
    }

    private val mGattHandlerCallback = object : ConnectionHandler.Callback {
        override fun onStateChanged(oldState: Int, newState: Int) {
            updateConnectionState()
            if (oldState != newState) {
                mCallbacks.forEach { it?.onGattStateChange(oldState, newState) }
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

    @SuppressLint("MissingPermission")
    override fun onInitialize(): Boolean {
        mHeadsetHandler = HeadsetHandler(context, mHeadsetCallback)
        mA2dpHandler = A2dpHandler(context, mA2dpHandlerCallback)
        mGattHandler = GattHandler(context, mGattHandlerCallback)
        context.registerReceiver(mBtStateChangeReceiver, IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"))
        context.registerReceiver(mBondStateReceiver, IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"))
        return true
    }

    fun isOriiConnected(): Boolean = mCurrentState == 2

    override fun start() {
        mHeadsetHandler?.start()
        mA2dpHandler?.start()
        mGattHandler?.start()
        routeManager.start()
    }

    @SuppressLint("MissingPermission")
    fun start(device: BluetoothDevice, callback: Callback) {
        mDevice = device
        mDeviceBonded = device.bondState == BluetoothDevice.BOND_BONDED
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback)
        }
        if (mDeviceBonded) {
            connectClassic()
        } else {
            pairOriiDevice()
        }
        start()
    }

    @SuppressLint("MissingPermission")
    fun connectClassic() {
        Log.d(TAG, "Call ConnectionManager connectClassic")
        if (mHeadsetHandler?.isConnected() != true && mHeadsetHandler?.isConnecting() != true) {
            mHeadsetHandler?.connect(mDevice)
        }
        if (mA2dpHandler?.isConnected() == true || mA2dpHandler?.isConnecting() == true) return
        mA2dpHandler?.connect(mDevice)
    }

    @SuppressLint("MissingPermission")
    private fun pairOriiDevice() {
        if (mDeviceBonded) return
        mDevice?.createBond()
    }

    fun unpairOriiDevice(device: BluetoothDevice?) {
        device?.let {
            try {
                it.javaClass.getMethod("removeBond").invoke(it)
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "")
            }
        }
    }

    override fun close() {
        mHeadsetHandler?.stop()
        mA2dpHandler?.stop()
        mGattHandler?.stop()
    }

    // Utilisation de coroutines pour la déconnexion complète
    @SuppressLint("MissingPermission")
    private suspend fun performDisconnectTask() {
        try {
            close()
            CommandManager.instance.close()
            routeManager.close()
            mGattHandler?.refreshGatt() // Méthode supposée présente dans GattHandler
            delay(3000)
            mGattHandler?.disconnect()
            delay(600)
            mGattHandler?.close()
            delay(600)
            mHeadsetHandler?.disconnect()
            delay(600)
            mA2dpHandler?.disconnect()
            delay(600)
            mDevice?.let { device ->
                if (device.bondState != BluetoothDevice.BOND_NONE) {
                    unpairOriiDevice(device)
                    delay(2000)
                } else {
                    Log.d(TAG, "Device bond state is none, cannot remove bond")
                }
            } ?: Log.d(TAG, "BT device not found")
            stopForegroundForConnection()
            mCurrentState = 0
            mCallbacks.forEach { it?.onOriiRemoveBond() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        scope.launch { performDisconnectTask() }
    }

    @SuppressLint("MissingPermission")
    private suspend fun performDisconnectBleTask() {
        try {
            close()
            CommandManager.instance.close()
            routeManager.close()
            mGattHandler?.refreshGatt()
            delay(600)
            mGattHandler?.disconnect()
            delay(600)
            mGattHandler?.close()
            delay(600)
            mCurrentState = 0
            stopForegroundForConnection()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnectBle() {
        scope.launch { performDisconnectBleTask() }
    }

    // Méthode disconnectClassic modernisée avec coroutine
    @SuppressLint("MissingPermission")
    private suspend fun performDisconnectClassic() {
        try {
            mHeadsetHandler?.disconnect()
            delay(600)
            mA2dpHandler?.disconnect()
            delay(600)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnectClassic() {
        scope.launch { performDisconnectClassic() }
    }

    @SuppressLint("MissingPermission")
    private suspend fun performStopSearch() {
        close()
        ScanManager.instance.close()
        CommandManager.instance.close()
        routeManager.close()
        stopForegroundForConnection()
    }

    fun stopSearch() {
        scope.launch { performStopSearch() }
    }

    fun updateConnectionState() {
        val headsetState = mHeadsetHandler?.currentState ?: 0
        val a2dpState = mA2dpHandler?.currentState ?: 0
        var gattState = mGattHandler?.currentState ?: 0
        Log.d(TAG, "updateConnectionState()-headsetState:$headsetState")
        Log.d(TAG, "updateConnectionState()-a2dpState:$a2dpState")
        Log.d(TAG, "updateConnectionState()-gattState:$gattState")
        if (headsetState == 2 && a2dpState == 2 && gattState == 0) {
            Log.d(TAG, "updateConnectionState()- device:${mDevice?.name}")
            Log.d(TAG, "updateConnectionState()- isConnecting():${mGattHandler?.isConnecting() ?: false}")
            Log.d(TAG, "updateConnectionState()-isConnectingGatt():${mGattHandler?.isConnectingGatt() ?: false}")
            Log.d(TAG, "updateConnectionState()-deviceBondState():${mDevice?.bondState}")
            if (mDevice != null &&
                !(mGattHandler?.isConnecting() ?: false) &&
                !(mGattHandler?.isConnected() ?: false) &&
                !(mGattHandler?.isConnectingGatt() ?: false) &&
                !(mGattHandler?.isClosingGatt() ?: false)
            ) {
                mGattHandler?.connect(mDevice)
                gattState = 1
            }
        }
        var newState = mCurrentState
        if (a2dpState == 0 || headsetState == 0) {
            connectClassic()
        }
        newState = if (gattState == 2 && a2dpState == 2 && headsetState == 2) {
            2
        } else {
            if (gattState != 1 && a2dpState != 1 && headsetState != 1) {
                if (gattState != 0 && a2dpState != 0 && headsetState != 0) newState else 0
            } else 1
        }
        Log.d(TAG, "Connection State: $mCurrentState=>$newState")
        if (newState != mCurrentState) {
            when (newState) {
                2 -> updateNotification(context.getString(R.string.notification_title_connected), "")
                1 -> {
                    startForegroundForConnection()
                    startCheckConnectionTimerTask()
                }
                0 -> stopForegroundForConnection()
            }
            mCallbacks.forEach { it?.onOriiStateChange(mCurrentState, newState) }
            mCurrentState = newState
        }
    }

    fun getConnectionState(): Int = mCurrentState

    fun getOriiAddress(): String = mDevice?.address ?: ""

    fun setNotificationIntentClass(cls: Class<*>) {
        mIntentClass = cls
    }

    private fun startForegroundForConnection() {
        Log.d(TAG, "startForegroundForConnection")
        // Pour startForeground(), assurez-vous que le service est déclaré avec foregroundServiceType dans le manifeste
        bluetoothService.startForeground(
            Constants.ORII_NOTIFICATION_CONNECTION_STATE,
            setNotification(context.getString(R.string.notification_title_connecting), "").build()
        )
    }

    @Synchronized
    fun updateNotificationBatteryLevel(i: Int) {
        updateNotification(
            context.getString(R.string.notification_title_connected),
            context.getString(R.string.notification_text_battery_level) + (i * 20) + "%"
        )
    }

    private fun setNotification(title: String, content: String): NotificationCompat.Builder {
        val channelId = context.getString(R.string.notification_connection_channel_id)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_statusbar)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setShowWhen(false)
        mIntentClass?.let { cls ->
            val intent = Intent(context, cls)
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(cls)
            stackBuilder.addNextIntent(intent)
            builder.setContentIntent(
                stackBuilder.getPendingIntent(
                    Constants.ORII_NOTIFICATION_CONNECTION_STATE,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    context.getString(R.string.notification_connection_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return builder
    }

    fun updateNotification(title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.notify(Constants.ORII_NOTIFICATION_CONNECTION_STATE, setNotification(title, content).build())
    }

    fun stopForegroundForConnection() {
        Log.d(TAG, "stopForegroundForConnection")
        bluetoothService.stopForeground(true)
    }

    private fun startCheckConnectionTimerTask() {
        checkConnectionJob?.cancel()
        checkConnectionJob = scope.launch {
            delay(180_000L)
            if (mCurrentState != 2) {
                stopSearch()
            }
        }
    }
}
