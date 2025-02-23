package com.origamilabs.orii.core.bluetooth.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TaskStackBuilder
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.app.NotificationCompat
import com.origamilabs.orii.core.Constants
import com.origamilabs.orii.core.R
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import com.origamilabs.orii.core.bluetooth.connection.A2dpHandler
import com.origamilabs.orii.core.bluetooth.connection.ConnectionHandler
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import com.origamilabs.orii.core.bluetooth.connection.HeadsetHandler
import java.util.*
import kotlin.concurrent.schedule

class ConnectionManager private constructor() : BaseManager() {

    companion object {
        private const val TAG = "ConnectionManager"
        val instance: ConnectionManager by lazy { ConnectionManager() }
    }

    private var mA2dpHandler: A2dpHandler? = null
    private var mCallTimer: Timer? = null
    private var mCurrentState: Int = 0
    private var mDevice: BluetoothDevice? = null
    private var mGattHandler: GattHandler? = null
    private var mHandler: Handler? = null
    private var mHandlerThread: HandlerThread? = null
    private var mHeadsetHandler: HeadsetHandler? = null
    private var mIntentClass: Class<*>? = null
    private var mDeviceBonded: Boolean = false

    // Liste des callbacks enregistrés
    private val mCallbacks: MutableList<Callback?> = ArrayList()

    // Observer l'état de liaison du device
    private val mBondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bluetoothDevice = intent?.getParcelableExtra<BluetoothDevice>("android.bluetooth.device.extra.DEVICE")
            if (bluetoothDevice == null || !BluetoothHelper.isOriiMacAddressInRange(bluetoothDevice.address)) return
            val bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1)
            Log.d(
                TAG,
                String.format(
                    "Bond state changed: %s => %s",
                    intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", -1),
                    bondState
                )
            )
            Log.d(TAG, "mDevice bind state:" + bluetoothDevice.bondState)
            when (bondState) {
                10, 11 -> mDeviceBonded = false
                12 -> {
                    mDeviceBonded = true
                    connectClassic()
                }
            }
        }
    }

    // Callback pour le headset
    private val mHeadsetCallback = object : ConnectionHandler.Callback {
        override fun OnStateChanged(oldState: Int, newState: Int) {
            updateConnectionState()
            if (oldState != newState) {
                mCallbacks.forEach { it?.onHeadsetStateChange(oldState, newState) }
            }
        }
    }

    // Callback pour A2DP
    private val mA2dpHandlerCallback = object : ConnectionHandler.Callback {
        override fun OnStateChanged(oldState: Int, newState: Int) {
            updateConnectionState()
            if (oldState != newState) {
                mCallbacks.forEach { it?.onA2dpStateChange(oldState, newState) }
            }
        }
    }

    // Callback pour GATT
    private val mGattHandlerCallback = object : ConnectionHandler.Callback {
        override fun OnStateChanged(oldState: Int, newState: Int) {
            updateConnectionState()
            if (oldState != newState) {
                mCallbacks.forEach { it?.onGattStateChange(oldState, newState) }
            }
        }
    }

    // Observer du changement d'état Bluetooth
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
        mHeadsetHandler = HeadsetHandler(mContext, mHeadsetCallback)
        mA2dpHandler = A2dpHandler(mContext, mA2dpHandlerCallback)
        mGattHandler = GattHandler(mContext, mGattHandlerCallback)
        mHandlerThread = HandlerThread("connection").also { it.start() }
        mHandler = Handler(mHandlerThread!!.looper)
        mContext.registerReceiver(mBtStateChangeReceiver, IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"))
        mContext.registerReceiver(mBondStateReceiver, IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"))
        return true
    }

    fun isOriiConnected(): Boolean = mCurrentState == 2

    override fun start() {
        mHeadsetHandler?.start()
        mA2dpHandler?.start()
        mGattHandler?.start()
        RouteManager.instance.start()
    }

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

    fun connectClassic() {
        Log.d(TAG, "Call ConnectionManager connectClassic")
        if (mHeadsetHandler?.isConnected() != true && mHeadsetHandler?.isConnecting() != true) {
            mHeadsetHandler?.connect(mDevice)
        }
        if (mA2dpHandler?.isConnected() == true || mA2dpHandler?.isConnecting() == true) return
        mA2dpHandler?.connect(mDevice)
    }

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

    // Tâche de déconnexion complète
    private inner class DisconnectTask : Runnable {
        override fun run() {
            try {
                close()
                CommandManager.instance.close()
                RouteManager.instance.close()
                mGattHandler?.refreshGatt()
                Thread.sleep(3000)
                mGattHandler?.disconnect()
                Thread.sleep(600)
                mGattHandler?.close()
                Thread.sleep(600)
                mHeadsetHandler?.disconnect()
                Thread.sleep(600)
                mA2dpHandler?.disconnect()
                Thread.sleep(600)
                mDevice?.let {
                    if (it.bondState != BluetoothDevice.BOND_NONE) {
                        unpairOriiDevice(it)
                        Thread.sleep(2000)
                    } else {
                        Log.d(TAG, "Device bond state is none, cannot remove bond")
                    }
                } ?: Log.d(TAG, "BT device can't found")
                stopForegroundForConnection()
                mCurrentState = 0
                mCallbacks.forEach { it?.onOriiRemoveBond() }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        mHandler?.post {
            try {
                // Utilise la tâche de déconnexion complète
                DisconnectTask().run()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    inner class StopSearchTask : Runnable {
        override fun run() {
            close()
            ScanManager.instance.close()
            CommandManager.instance.close()
            RouteManager.instance.close()
            stopForegroundForConnection()
        }
    }

    fun stopSearch() {
        mHandler?.post {
            close()
            ScanManager.instance.close()
            CommandManager.instance.close()
            RouteManager.instance.close()
            stopForegroundForConnection()
        }
    }

    private inner class DisconnectBleTask : Runnable {
        override fun run() {
            try {
                close()
                CommandManager.instance.close()
                RouteManager.instance.close()
                mGattHandler?.refreshGatt()
                Thread.sleep(600)
                mGattHandler?.disconnect()
                Thread.sleep(600)
                mGattHandler?.close()
                Thread.sleep(600)
                mCurrentState = 0
                stopForegroundForConnection()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun disconnectBle() {
        mHandler?.post {
            try {
                DisconnectBleTask().run()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun updateConnectionState() {
        val headsetState = mHeadsetHandler?.currentState ?: 0
        val a2dpState = mA2dpHandler?.currentState ?: 0
        var gattState = mGattHandler?.currentState ?: 0
        Log.d(TAG, "updateConnectionState()-headsetState:$headsetState")
        Log.d(TAG, "updateConnectionState()-a2dpState:$a2dpState")
        Log.d(TAG, "updateConnectionState()-gattState:$gattState")
        if (headsetState == 2 && a2dpState == 2 && gattState == 0) {
            Log.d(TAG, "updateConnectionState()- device:" + mDevice?.name)
            Log.d(TAG, "updateConnectionState()- isConnecting():" + (mGattHandler?.isConnecting() ?: false))
            Log.d(TAG, "updateConnectionState()-isConnectingGatt():" + (mGattHandler?.isConnectingGatt() ?: false))
            Log.d(TAG, "updateConnectionState()-deviceBondState():" + mDevice?.bondState)
            if (mDevice != null && !(mGattHandler?.isConnecting() ?: false) && !(mGattHandler?.isConnected() ?: false)
                && !(mGattHandler?.isConnectingGatt() ?: false) && !(mGattHandler?.isClosingGatt() ?: false)
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
                2 -> updateNotification(mContext.getString(R.string.notification_title_connected), "")
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
        mBluetoothService.startForeground(
            Constants.ORII_NOTIFICATION_CONNECTION_STATE,
            setNotification(mContext.getString(R.string.notification_title_connecting), "").build()
        )
    }

    @Synchronized
    fun updateNotificationBatteryLevel(i: Int) {
        updateNotification(
            mContext.getString(R.string.notification_title_connected),
            mContext.getString(R.string.notification_text_battery_level) + (i * 20) + "%"
        )
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
            builder.setContentIntent(
                stackBuilder.getPendingIntent(
                    Constants.ORII_NOTIFICATION_CONNECTION_STATE,
                    134217728
                )
            )
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
}
