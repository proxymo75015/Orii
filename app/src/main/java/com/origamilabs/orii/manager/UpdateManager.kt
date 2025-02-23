package com.origamilabs.orii.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import com.orii.gaiacontrol.gaia.MainGaiaManager
import com.orii.gaiacontrol.services.BluetoothService
import com.orii.gaiacontrol.services.GAIABREDRService
import com.orii.libraries.vmupgrade.ORiiUpgradeError
import com.orii.libraries.vmupgrade.ORiiUploadProgress
import java.io.File
import java.util.*
import kotlin.math.roundToInt

object UpdateManager : MainGaiaManager.MainGaiaManagerListener {
    private const val BLUETOOTH_ADDRESS_KEY = "Device Bluetooth address"
    private const val BR_EDR = 1
    private const val CONNECTED = 2
    private const val CONNECTING = 1
    private const val CONNECTION_STATE_HAS_CHANGED = 0
    private const val DEVICE_BOND_STATE_HAS_CHANGED = 1
    private const val DISCONNECTED = 0
    private const val DISCONNECTING = 3
    private const val GAIA_PACKET = 3
    private const val GAIA_READY = 4
    private const val GATT_MESSAGE = 6
    private const val GATT_READY = 5
    private const val GATT_SUPPORT = 2
    private const val INIT_GAIA_MANAGER = 99
    private const val TAG = "UpdateManager"
    private const val UPGRADE_ERROR = 3
    private const val UPGRADE_FINISHED = 0
    private const val UPGRADE_MESSAGE = 7
    private const val UPGRADE_REQUEST_CONFIRMATION = 1
    private const val UPGRADE_STEP_HAS_CHANGED = 2
    private const val UPGRADE_UPLOAD_PROGRESS = 4

    private var context: Context? = null
    private var handler: UpdateHandler? = null
    private var isUpgrading: Boolean = false
    private var mGaiaManager: MainGaiaManager? = null
    private var mService: BluetoothService? = null
    private var mServiceConnection: ServiceConnection? = null
    private var macAddress: String? = null
    private var onUpdateListener: OnUpdateListener? = null
    private var targetVersion: Int = -1

    interface OnUpdateListener {
        fun onProgressChanged(progress: Int)
        fun onUpdateFinished()
        fun onUpdateReady()
    }

    private class UpdateHandler(val context: Context) : Handler() {
        override fun handleMessage(msg: Message) {
            UpdateManager.handleMessageFromService(context, msg)
        }
    }

    fun init(context: Context, macAddress: String, version: Int, onUpdateListener: OnUpdateListener) {
        requireNotNull(context) { "context must not be null" }
        requireNotNull(macAddress) { "macAddress must not be null" }
        requireNotNull(onUpdateListener) { "onUpdateListener must not be null" }
        this.context = context
        this.macAddress = macAddress
        targetVersion = version
        this.onUpdateListener = onUpdateListener
        handler = UpdateHandler(context)

        if (mServiceConnection == null) {
            mServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName, serviceBinder: IBinder) {
                    Log.d("UpdateManager", "onServiceConnected()")
                    if (componentName.className == GAIABREDRService::class.java.name) {
                        mService = (serviceBinder as GAIABREDRService.LocalBinder).service
                        connect()
                    }
                    handler?.let { mService?.addHandler(it) }
                }

                override fun onServiceDisconnected(componentName: ComponentName) {
                    Log.d("UpdateManager", "onServiceDisconnected()")
                }
            }
            val intent = Intent(context, GAIABREDRService::class.java).apply {
                putExtra(BLUETOOTH_ADDRESS_KEY, macAddress)
            }
            context.bindService(intent, mServiceConnection!!, Context.BIND_AUTO_CREATE)
        } else {
            connect()
            handler?.let { mService?.addHandler(it) }
        }
    }

    fun connect() {
        mService?.let { service ->
            when (service.connectionState) {
                DISCONNECTED -> {
                    Log.d(TAG, "connectToDevice: $macAddress")
                    service.connectToDevice(macAddress ?: throw UninitializedPropertyAccessException("macAddress"))
                }
                CONNECTED -> {
                    Log.d(TAG, "Already connected.")
                    startUpdate()
                }
            }
        }
        mGaiaManager = MainGaiaManager(this, 1)
    }

    fun updateFirmware(context: Context) {
        if (mService?.connectionState == CONNECTED && mService?.isGaiaReady() == true) {
            Log.v(TAG, "connectToDevice: $macAddress")
            val file = File(context.getExternalFilesDir("update"), "v$targetVersion.bin")
            Log.v(TAG, "Firmware File: $file")
            if (file.exists() && !isUpgrading) {
                Log.v(TAG, "startUpgrade")
                isUpgrading = true
                mService?.startUpgrade(file)
            } else {
                Log.v(TAG, "Firmware file not exist: $file")
            }
        }
    }

    fun startUpdate() {
        if (mService?.isUpgrading() == true) return
        handler?.postDelayed(
            { updateFirmware(context ?: throw UninitializedPropertyAccessException("context")) },
            500L
        )
    }

    fun stopUpdate() {
        mService?.abortUpgrade()
        isUpgrading = false
    }

    fun handleMessageFromService(context: Context, msg: Message) {
        requireNotNull(context) { "context must not be null" }
        requireNotNull(msg) { "msg must not be null" }
        when (msg.what) {
            CONNECTION_STATE_HAS_CHANGED -> {
                val state = msg.obj as? Int ?: throw TypeCastException("null cannot be cast to Int")
                Log.d(TAG, "CONNECTION_STATE_HAS_CHANGED: " + when (state) {
                    DISCONNECTED -> "DISCONNECTED"
                    CONNECTING -> "CONNECTING"
                    CONNECTED -> "CONNECTED"
                    DISCONNECTING -> "DISCONNECTING"
                    else -> "UNKNOWN"
                })
            }
            DEVICE_BOND_STATE_HAS_CHANGED -> {
                val state = msg.obj as? Int ?: throw TypeCastException("null cannot be cast to Int")
                Log.d(TAG, "DEVICE_BOND_STATE_HAS_CHANGED: " + when (state) {
                    12 -> "BONDED"
                    11 -> "BONDING"
                    else -> "BOND NONE"
                })
            }
            2 -> Log.d(TAG, "GATT_SUPPORT")
            3 -> {
                val bytes = msg.obj as? ByteArray ?: throw TypeCastException("null cannot be cast to ByteArray")
                mGaiaManager?.onReceiveGAIAPacket(bytes)
            }
            4 -> {
                Log.d(TAG, "GAIA_READY")
                getInformationFromDevice()
                Log.d(TAG, "isUpgrading: ${mService?.isUpgrading()}")
                onUpdateListener?.onUpdateReady()
            }
            5 -> Log.d(TAG, "GATT_READY")
            7 -> onReceiveUpgradeMessage(msg.arg1, msg.obj)
            else -> Log.d(TAG, "UNKNOWN MESSAGE: ${msg.what}")
        }
    }

    private fun getRSSINotifications(notify: Boolean) {
        if (notify) {
            if (mService?.startRssiUpdates(true) != true) {
                mGaiaManager?.getNotifications(2, true)
            }
        } else {
            mService?.startRssiUpdates(false)
            mGaiaManager?.getNotifications(2, false)
        }
    }

    private fun getInformationFromDevice() {
        if (mService?.connectionState != CONNECTED) return
        if (mService?.isGaiaReady() == true) {
            mGaiaManager?.getInformation(3)
            mGaiaManager?.getInformation(2)
            mGaiaManager?.getInformation(1)
            mGaiaManager?.getNotifications(1, true)
            getRSSINotifications(true)
        }
    }

    override fun sendGAIAPacket(bytes: ByteArray): Boolean {
        requireNotNull(bytes) { "bytes must not be null" }
        return mService?.sendGAIAPacket(bytes) ?: false
    }

    override fun onChargerConnected(b: Boolean) {
        Log.d(TAG, "onChargerConnected: $b")
    }

    override fun onGetBatteryLevel(i: Int) {
        Log.d(TAG, "onGetBatteryLevel: $i")
    }

    override fun onGetRSSILevel(i: Int) {
        Log.d(TAG, "onGetRSSILevel: $i")
    }

    override fun onGetAPIVersion(i: Int, i1: Int, i2: Int) {
        Log.d(TAG, "onGetAPIVersion: $i $i1 $i2")
    }

    private fun onReceiveUpgradeMessage(message: Int, content: Any?) {
        when (message) {
            UPGRADE_FINISHED -> {
                Log.d(TAG, "UPGRADE_FINISHED: ")
                mService?.removeHandler(handler)
                onUpdateListener?.onUpdateFinished()
            }
            UPGRADE_REQUEST_CONFIRMATION -> {
                Log.d(TAG, "UPGRADE_REQUEST_CONFIRMATION. We force true here")
                val value = content as? Int ?: throw TypeCastException("null cannot be cast to Int")
                mService?.sendConfirmation(value, true)
            }
            UPGRADE_STEP_HAS_CHANGED -> Log.d(TAG, "UPGRADE_STEP_HAS_CHANGED: ")
            UPGRADE_UPLOAD_PROGRESS -> {
                val progress = content as? ORiiUploadProgress
                    ?: throw TypeCastException("null cannot be cast to ORiiUploadProgress")
                Log.d(TAG, "UPGRADE_UPLOAD_PROGRESS: ${progress.percentage} time remain: ${getStringForTime(progress.remainingTime)}")
                onUpdateListener?.onProgressChanged(progress.percentage.roundToInt())
            }
            UPGRADE_ERROR -> {
                val error = content as? ORiiUpgradeError
                    ?: throw TypeCastException("null cannot be cast to ORiiUpgradeError")
                manageError(error)
            }
        }
        if (message != UPGRADE_UPLOAD_PROGRESS) {
            Log.d(TAG, "NOT UPGRADE_UPLOAD_PROGRESS")
        }
    }

    private fun manageError(error: ORiiUpgradeError) {
        when (error.error) {
            1 -> Log.d(TAG, "The board is not ready for an upgrade, please try again later.")
            2 -> Log.d(TAG, "A protocol exception occurred during the upgrade. Please try again later or contact your application provider.")
            3 -> Log.d(TAG, "Error from the board. Error Code: ${error.returnCode}")
            4 -> Log.d(TAG, "An exception occurred during the upgrade. Please try again later or contact your application provider.")
            5 -> Log.d(TAG, "An upgrade is already processing")
            6 -> Log.d(TAG, "File error")
        }
    }

    private fun getStringForTime(time: Long): String {
        var seconds = time / 1000
        val minute = 60L
        return if (seconds < minute) {
            "${seconds - (seconds % 5)}s"
        } else {
            val minutes = seconds / minute
            val remainingSeconds = seconds - (minutes * minute)
            val deca = 10L
            if (minutes < deca) {
                val reduced = remainingSeconds - (remainingSeconds % deca)
                if (reduced == 0L) {
                    "${minutes}min"
                } else {
                    "${minutes}min${reduced}s"
                }
            } else {
                if (remainingSeconds >= 30L) {
                    // minutes is incremented
                    seconds = minutes + 1
                }
                if (minutes < 60L) {
                    "${minutes}min"
                } else {
                    val hours = minutes / 60
                    val remainingMinutes = minutes - (60 * hours)
                    if (hours < 12) {
                        val reducedMinutes = remainingMinutes - (remainingMinutes % 5)
                        if (reducedMinutes == 0L) {
                            "${hours}h"
                        } else {
                            "${hours}h${reducedMinutes}min"
                        }
                    } else {
                        if (hours < 24L) {
                            "${hours}h${remainingMinutes - (remainingMinutes % 30)}min"
                        } else {
                            if (remainingMinutes > 30L) {
                                // hours is incremented
                                "${hours + 1}h"
                            } else {
                                "${hours}h"
                            }
                        }
                    }
                }
            }
        }
    }

    override fun sendGAIAPacket(bytes: ByteArray): Boolean = mService?.sendGAIAPacket(bytes) ?: false

    override fun onChargerConnected(b: Boolean) {
        Log.d(TAG, "onChargerConnected: $b")
    }

    override fun onGetBatteryLevel(i: Int) {
        Log.d(TAG, "onGetBatteryLevel: $i")
    }

    override fun onGetRSSILevel(i: Int) {
        Log.d(TAG, "onGetRSSILevel: $i")
    }

    override fun onGetAPIVersion(i: Int, i1: Int, i2: Int) {
        Log.d(TAG, "onGetAPIVersion: $i $i1 $i2")
    }
}
