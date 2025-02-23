package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.origamilabs.orii.api.Config
import com.origamilabs.orii.api.request.OriiRequest
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.VolleyLog
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.android.volley.Response
import com.android.volley.VolleyError
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class CommandManager private constructor() : BaseManager() {

    companion object {
        private const val TAG = "CommandManager"
        const val ACTION_BATTERY_LEVEL = "com.origamilabs.orii.ACTION_BATTERY_LEVEL"
        const val ACTION_CHECK_GESTURE_MODE = "com.origamilabs.orii.ACTION_CHECK_GESTURE_MODE"
        const val ACTION_CHECK_LANGUAGE = "com.origamilabs.orii.ACTION_CHECK_LANGUAGE"
        const val ACTION_CHECK_MIC_MODE = "com.origamilabs.orii.ACTION_CHECK_MIC_MODE"
        const val ACTION_CHECK_SENSITIVITY_OF_GESTURE = "com.origamilabs.orii.ACTION_CHECK_SENSITIVITY_OF_GESTURE"
        const val ACTION_DOUBLE_BUTTON_PRESSED = "com.origamilabs.orii.ACTION_DOUBLE_BUTTON_PRESSED"
        const val ACTION_FIRMWARE_VERSION = "com.origamilabs.orii.ACTION_FIRMWARE_VERSION"
        const val ACTION_GESTURE_FLAT_TRIPLE_TAP = "com.origamilabs.orii.ACTION_GESTURE_FLAT_TRIPLE_TAP"
        const val ACTION_GESTURE_REVERSE_DOUBLE_TAP = "com.origamilabs.orii.ACTION_GESTURE_REVERSE_DOUBLE_TAP"
        const val ACTION_GESTURE_SIDE_DOUBLE_TAP = "com.origamilabs.orii.ACTION_GESTURE_SIDE_DOUBLE_TAP"
        const val ACTION_MIC_MODE_CHANGED = "com.origamilabs.orii.ACTION_MIC_MODE_CHANGED"
        const val ACTION_SINGLE_BUTTON_DOUBLE_PRESSED = "com.origamilabs.orii.ACTION_SINGLE_BUTTON_DOUBLE_PRESSED"
        const val ACTION_SINGLE_BUTTON_LONG_PRESSED = "com.origamilabs.orii.ACTION_SINGLE_BUTTON_LONG_PRESSED"
        const val ACTION_SINGLE_BUTTON_PRESSED = "com.origamilabs.orii.ACTION_SINGLE_BUTTON_PRESSED"
        const val ACTION_VOICE_ASSISTANT_COUNTER = "com.origamilabs.orii.ACTION_VOICE_ASSISTANT_COUNTER"
        const val ACTION_VOICE_ASSISTANT_STATE_CHANGED = "com.origamilabs.orii.ACTION_VOICE_ASSISTANT_STATE_CHANGED"
        const val EXTRA_DATA = "com.origamilabs.orii.EXTRA_DATA"

        private val ORII_PROFILE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        private val ORII_NOTIFY_1_UUID: UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")
        private val ORII_WRITE_UUID: UUID = UUID.fromString("0000FFF6-0000-1000-8000-00805F9B34FB")
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        val instance: CommandManager by lazy { CommandManager() }
    }

    private var mBatteryLevel: Int = 0
    private var mFirmwareVersion: Int = 0
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattHandler: GattHandler? = null
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var mWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var mIsWritingCharacteristicToDevice: Boolean = false

    private val mFixedTaskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val mUnfixedTaskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val mCallbacks: MutableList<Callback> = ArrayList()

    interface Callback {
        fun onDataReceived(intent: Intent)
    }

    private val mConnectionCallback = object : ConnectionManager.Callback {
        override fun onA2dpStateChange(prevState: Int, newState: Int) {}
        override fun onGattStateChange(prevState: Int, newState: Int) {}
        override fun onHeadsetStateChange(prevState: Int, newState: Int) {}
        override fun onOriiRemoveBond() {}

        override fun onOriiStateChange(prevState: Int, newState: Int) {
            if (newState == 2) {
                putCallCheckLanguageTask()
                putCallBatteryLevelTaskToUnfixedTaskQueue()
                putCallFirmwareVersionTaskToUnfixedTaskQueue()
                putCallVoiceAssistantCounterTask()
                putCallCheckMicModeTask()
                putCallCheckTestModeTask()
            }
        }
    }

    private var windowing: Boolean = false

    // onInitialize() de BaseManager
    override fun onInitialize(): Boolean = false

    fun addCallback(callback: Callback) {
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback)
        }
    }

    fun removeCallback(callback: Callback) {
        mCallbacks.remove(callback)
    }

    fun getBatteryLevel(): Int = mBatteryLevel
    fun getFirmwareVersion(): Int = mFirmwareVersion

    fun start(gattHandler: GattHandler, bluetoothGatt: BluetoothGatt) {
        mGattHandler = gattHandler
        mBluetoothGatt = bluetoothGatt
        ConnectionManager.getInstance().addCallback(mConnectionCallback)
        val service: BluetoothGattService? = mBluetoothGatt?.getService(ORII_PROFILE_UUID)
        if (service == null) {
            Log.d(TAG, "profileService is null")
            mGattHandler?.disconnect()
            return
        }
        Log.d(TAG, "profileService: $service")
        mWriteCharacteristic = service.getCharacteristic(ORII_WRITE_UUID)
        mNotifyCharacteristic = service.getCharacteristic(ORII_NOTIFY_1_UUID)
        setCharacteristicNotification(mNotifyCharacteristic, true)
        start()
    }

    override fun start() {
        Log.d(TAG, "start()")
        val handler = Handler(Looper.getMainLooper())
        mCallTimer?.cancel()
        mCallTimer?.purge()
        mCallTimer = null
        mIsWritingCharacteristicToDevice = false
        mFixedTaskQueue.clear()
        mUnfixedTaskQueue.clear()

        mCallTimer = Timer()
        mCallTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (mFixedTaskQueue.isEmpty()) {
                    putCallBatteryLevelTaskToFixedTaskQueue()
                    putCallFirmwareVersionTaskToFixedTaskQueue()
                }
                if (!mIsWritingCharacteristicToDevice) {
                    try {
                        handler.post(mFixedTaskQueue.take())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                Log.d(TAG, "Dispatch fixed task after count size: ${mFixedTaskQueue.size}")
            }
        }, 1000L, 30000L)

        mCallTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (mUnfixedTaskQueue.isNotEmpty() && !mIsWritingCharacteristicToDevice) {
                    try {
                        handler.post(mUnfixedTaskQueue.take())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                Log.d(TAG, "Dispatch unfixed task after count size: ${mUnfixedTaskQueue.size}")
            }
        }, 1000L, 1000L)
    }

    override fun close() {
        Log.d(TAG, "close()")
        mBatteryLevel = -1
        mFirmwareVersion = -1
        mGattHandler = null
        ConnectionManager.getInstance().removeCallback(mConnectionCallback)
        mCallTimer?.cancel()
        mCallTimer?.purge()
        mCallTimer = null
        mIsWritingCharacteristicToDevice = false
        mFixedTaskQueue.clear()
        mUnfixedTaskQueue.clear()
    }

    fun getColorCode(code: Int): Byte {
        val result: Byte = when (code) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            else -> 0
        }.toByte()
        Log.d(TAG, "Color code: " + String.format("0x%02X", result))
        return result
    }

    fun getVibrationCode(code: Int): Byte {
        val result: Byte = when (code) {
            0 -> 0
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 1
            else -> 1
        }.toByte()
        Log.d(TAG, "Vibration  code: " + String.format("0x%02X", result))
        return result
    }

    @Synchronized
    fun writeCharacteristic(bytes: ByteArray): Boolean {
        mIsWritingCharacteristicToDevice = true
        return if (getBluetoothAdapter() != null && mBluetoothGatt != null) {
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(String.format("0x%02X ", b))
            }
            Log.d(TAG, "Written characteristic: $sb")
            mWriteCharacteristic?.value = bytes
            val result = mBluetoothGatt!!.writeCharacteristic(mWriteCharacteristic)
            Log.d(TAG, "writeCharacteristic result: $result")
            mIsWritingCharacteristicToDevice = false
            result
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized")
            mIsWritingCharacteristicToDevice = false
            false
        }
    }

    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic?, enabled: Boolean) {
        if (getBluetoothAdapter() == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        val descriptor = characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
        if (enabled) {
            descriptor?.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        } else {
            descriptor?.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        mBluetoothGatt!!.writeDescriptor(descriptor)
    }

    fun sendCommand(cmd: Byte, length: Byte, data: ByteArray?): Boolean {
        val packet = ByteArray((length + 4).toInt())
        packet[0] = (-86).toByte()
        packet[1] = cmd
        packet[2] = length
        packet[packet.size - 1] = (-1).toByte()
        data?.let { System.arraycopy(it, 0, packet, 3, length.toInt()) }
        return writeCharacteristic(packet)
    }

    fun callback(characteristic: BluetoothGattCharacteristic) {
        val value = characteristic.value
        if (value.size <= 4) return
        val header = value[0]
        val cmd = value[1]
        val len = value[2]
        val payload = value.copyOfRange(3, (len + 3) and 0xFF)
        val end = value[(len and 0xFF) + 3]
        val sb = StringBuilder()
        for (b in value) {
            sb.append(String.format("%02X ", b))
        }
        Log.d(TAG, "${characteristic.uuid} data: $sb")
        Log.d(TAG, "Header: " + String.format("0x%02X", header) + " End: " + String.format("0x%02X", end))
        if (header != (-86).toByte() || end != (-1).toByte()) {
            Log.e(TAG, "GATT_FAILURE")
        } else {
            handleCommand(cmd, payload)
        }
    }

    private fun handleCommand(cmd: Byte, payload: ByteArray) {
        val intent = Intent()
        when (cmd) {
            (-112).toByte() -> {
                Log.d(TAG, "changed test mode to : " + String.format("0x%02X", payload[0]) + " : " + payload[0].toInt())
            }
            (-108).toByte() -> {
                val testMode = payload[0].toInt()
                Log.d(TAG, "test mode is : " + String.format("0x%02X", payload[0]) + " : $testMode")
                if (testMode == 2) {
                    putCallSwitchTestModeTask()
                }
            }
            64.toByte() -> {
                intent.action = ACTION_GESTURE_SIDE_DOUBLE_TAP
                Log.d(TAG, "Side double tap trigger ORII gesture")
            }
            7.toByte() -> {
                val batLevel = payload[0].toInt()
                Log.d(TAG, "battery level: " + String.format("0x%02X", payload[0]) + " : $batLevel")
                mBatteryLevel = batLevel
                intent.action = ACTION_BATTERY_LEVEL
                intent.putExtra(EXTRA_DATA, batLevel)
            }
            8.toByte() -> {
                val fwVersion = payload[0].toInt()
                Log.d(TAG, "firmware version: " + String.format("0x%02X", payload[0]) + " : $fwVersion")
                mFirmwareVersion = fwVersion
                intent.action = ACTION_FIRMWARE_VERSION
                intent.putExtra(EXTRA_DATA, fwVersion)
            }
            48.toByte() -> {
                val micMode = payload[0].toInt()
                intent.action = ACTION_CHECK_MIC_MODE
                intent.putExtra(EXTRA_DATA, micMode)
                Log.d(TAG, "mic mode is : " + String.format("0x%02X", payload[0]) + " : $micMode")
            }
            49.toByte() -> {
                val newMicMode = payload[0].toInt()
                intent.action = ACTION_MIC_MODE_CHANGED
                intent.putExtra(EXTRA_DATA, newMicMode)
                Log.d(TAG, "mic mode changed to : " + String.format("0x%02X", payload[0]) + " : $newMicMode")
            }
            51.toByte() -> {
                intent.action = ACTION_VOICE_ASSISTANT_STATE_CHANGED
                intent.putExtra(EXTRA_DATA, payload[0].toInt())
                Log.d(TAG, "Voice assistant start/end : " + String.format("0x%02X", payload[0]) + " : " + payload[0].toInt())
            }
            52.toByte() -> {
                intent.action = ACTION_VOICE_ASSISTANT_COUNTER
                intent.putExtra(EXTRA_DATA, payload[0].toInt())
                Log.d(TAG, "Voice assistant counter : " + String.format("0x%02X", payload[0]) + " : " + payload[0].toInt())
            }
            54.toByte() -> {
                val lang = payload[0].toInt()
                intent.action = ACTION_CHECK_LANGUAGE
                intent.putExtra(EXTRA_DATA, lang)
               
