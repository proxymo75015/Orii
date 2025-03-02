package com.origamilabs.orii.core.bluetooth.manager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.stetho.dumpapp.Framer
import com.origamilabs.orii.api.Config
import com.origamilabs.orii.api.request.OriiRequest
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandManager @Inject constructor(
    private val connectionManager: ConnectionManager
) : BaseManager() {

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

        private val ORII_PROFILE_UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        private val ORII_NOTIFY_1_UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")
        private val ORII_WRITE_UUID = UUID.fromString("0000FFF6-0000-1000-8000-00805F9B34FB")
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }

    // Propriétés Bluetooth et état
    private var batteryLevel: Int = 0
    private var firmwareVersion: Int = 0
    private var bluetoothGatt: BluetoothGatt? = null
    private var gattHandler: GattHandler? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var isWritingCharacteristicToDevice: Boolean = false

    // Utilisation de channels pour les tâches fixes et non fixes
    private val fixedTaskChannel = Channel<() -> Unit>(Channel.UNLIMITED)
    private val unfixedTaskChannel = Channel<() -> Unit>(Channel.UNLIMITED)

    // Création d'un scope dédié aux tâches
    private val taskScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var fixedTaskJob: Job? = null
    private var unfixedTaskJob: Job? = null

    private val callbacks = mutableListOf<Callback>()

    interface Callback {
        fun onDataReceived(intent: Intent)
    }

    private val connectionCallback = object : ConnectionManager.Callback {
        override fun onA2dpStateChange(prevState: Int, newState: Int) {}
        override fun onGattStateChange(prevState: Int, newState: Int) {}
        override fun onHeadsetStateChange(prevState: Int, newState: Int) {}
        override fun onOriiRemoveBond() {}
        override fun onOriiStateChange(prevState: Int, newState: Int) {
            if (newState == 2) {
                putCallCheckLanguageTask()
                putCallBatteryLevelTaskToUnfixedChannel()
                putCallFirmwareVersionTaskToUnfixedChannel()
                putCallVoiceAssistantCounterTask()
                putCallCheckMicModeTask()
                putCallCheckTestModeTask()
            }
        }
    }

    private var windowing = false

    override fun onInitialize(): Boolean = false

    fun addCallback(callback: Callback) {
        if (!callbacks.contains(callback)) callbacks.add(callback)
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    fun getBatteryLevel() = batteryLevel
    fun getFirmwareVersion() = firmwareVersion

    @SuppressLint("MissingPermission")
    fun start(gattHandler: GattHandler, bluetoothGatt: BluetoothGatt) {
        this.gattHandler = gattHandler
        this.bluetoothGatt = bluetoothGatt
        connectionManager.addCallback(connectionCallback)
        val service: BluetoothGattService? = bluetoothGatt.getService(ORII_PROFILE_UUID)
        if (service == null) {
            Log.d(TAG, "profileService is null")
            gattHandler.disconnect()
            return
        }
        Log.d(TAG, "profileService: $service")
        writeCharacteristic = service.getCharacteristic(ORII_WRITE_UUID)
        notifyCharacteristic = service.getCharacteristic(ORII_NOTIFY_1_UUID)
        setCharacteristicNotification(notifyCharacteristic, true)
        startTaskSchedulers()
    }

    /**
     * Démarre les coroutines qui gèrent l'exécution périodique des tâches.
     */
    private fun startTaskSchedulers() {
        fixedTaskJob?.cancel()
        unfixedTaskJob?.cancel()
        // Tâches fixes : tous les 30 secondes (après un délai initial)
        fixedTaskJob = taskScope.launch {
            delay(1000L)
            while (isActive) {
                if (fixedTaskChannel.isEmpty) {
                    putCallBatteryLevelTaskToFixedChannel()
                    putCallFirmwareVersionTaskToFixedChannel()
                }
                if (!isWritingCharacteristicToDevice) {
                    // Exécute la prochaine tâche fixe (suspend jusqu'à réception)
                    val task = fixedTaskChannel.receive()
                    task.invoke()
                }
                Log.d(TAG, "Dispatch fixed task")
                delay(30000L)
            }
        }
        // Tâches non fixes : toutes les secondes
        unfixedTaskJob = taskScope.launch {
            delay(1000L)
            while (isActive) {
                if (!isWritingCharacteristicToDevice && !unfixedTaskChannel.isEmpty) {
                    val task = unfixedTaskChannel.receive()
                    task.invoke()
                }
                Log.d(TAG, "Dispatch unfixed task")
                delay(1000L)
            }
        }
    }

    override fun close() {
        Log.d(TAG, "close()")
        batteryLevel = -1
        firmwareVersion = -1
        gattHandler = null
        connectionManager.removeCallback(connectionCallback)
        fixedTaskJob?.cancel()
        unfixedTaskJob?.cancel()
        taskScope.cancel()
        // Ferme les channels
        fixedTaskChannel.cancel()
        unfixedTaskChannel.cancel()
    }

    fun getColorCode(code: Int): Byte {
        val result = when (code) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            else -> 0
        }.toByte()
        Log.d(TAG, "Color code: ${String.format("0x%02X", result)}")
        return result
    }

    fun getVibrationCode(code: Int): Byte {
        val result = when (code) {
            0 -> 0
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            else -> 1
        }.toByte()
        Log.d(TAG, "Vibration code: ${String.format("0x%02X", result)}")
        return result
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun writeCharacteristic(bytes: ByteArray): Boolean {
        isWritingCharacteristicToDevice = true
        return if (getBluetoothAdapter() != null && bluetoothGatt != null) {
            val sb = bytes.joinToString(" ") { String.format("0x%02X", it) }
            Log.d(TAG, "Written characteristic: $sb")
            writeCharacteristic?.value = bytes
            val result = bluetoothGatt!!.writeCharacteristic(writeCharacteristic)
            Log.d(TAG, "writeCharacteristic result: $result")
            isWritingCharacteristicToDevice = false
            result
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized")
            isWritingCharacteristicToDevice = false
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic?, enabled: Boolean) {
        if (getBluetoothAdapter() == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        val descriptor = characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
        descriptor?.value = if (enabled)
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        else
            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        bluetoothGatt!!.writeDescriptor(descriptor)
    }

    @SuppressLint("MissingPermission")
    fun sendCommand(cmd: Byte, length: Byte, data: ByteArray?): Boolean {
        val packet = ByteArray((length + 4).toInt()).apply {
            this[0] = (-86).toByte()
            this[1] = cmd
            this[2] = length
            this[this.size - 1] = (-1).toByte()
            data?.let { System.arraycopy(it, 0, this, 3, length.toInt()) }
        }
        return writeCharacteristic(packet)
    }

    @SuppressLint("MissingPermission")
    fun callback(characteristic: BluetoothGattCharacteristic) {
        val value = characteristic.value
        if (value.size <= 4) return
        val header = value[0]
        val cmd = value[1]
        val len = value[2]
        val payload = value.copyOfRange(3, ((len.toInt() + 3) and 0xFF))
        val end = value[((len.toInt() and 0xFF) + 3)]
        val sb = value.joinToString(" ") { String.format("%02X", it) }
        Log.d(TAG, "${characteristic.uuid} data: $sb")
        Log.d(TAG, "Header: ${String.format("0x%02X", header)} End: ${String.format("0x%02X", end)}")
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
                Log.d(TAG, "changed test mode to : ${String.format("0x%02X", payload[0])} : ${payload[0].toInt()}")
            }
            (-108).toByte() -> {
                val testMode = payload[0].toInt()
                Log.d(TAG, "test mode is : ${String.format("0x%02X", payload[0])} : $testMode")
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
                Log.d(TAG, "battery level: ${String.format("0x%02X", payload[0])} : $batLevel")
                batteryLevel = batLevel
                intent.action = ACTION_BATTERY_LEVEL
                intent.putExtra(EXTRA_DATA, batLevel)
            }
            8.toByte() -> {
                val fwVersion = payload[0].toInt()
                Log.d(TAG, "firmware version: ${String.format("0x%02X", payload[0])} : $fwVersion")
                firmwareVersion = fwVersion
                intent.action = ACTION_FIRMWARE_VERSION
                intent.putExtra(EXTRA_DATA, fwVersion)
            }
            48.toByte() -> {
                val micMode = payload[0].toInt()
                intent.action = ACTION_CHECK_MIC_MODE
                intent.putExtra(EXTRA_DATA, micMode)
                Log.d(TAG, "mic mode is : ${String.format("0x%02X", payload[0])} : $micMode")
            }
            49.toByte() -> {
                val newMicMode = payload[0].toInt()
                intent.action = ACTION_MIC_MODE_CHANGED
                intent.putExtra(EXTRA_DATA, newMicMode)
                Log.d(TAG, "mic mode changed to : ${String.format("0x%02X", payload[0])} : $newMicMode")
            }
            51.toByte() -> {
                intent.action = ACTION_VOICE_ASSISTANT_STATE_CHANGED
                intent.putExtra(EXTRA_DATA, payload[0].toInt())
                Log.d(TAG, "Voice assistant start/end : ${String.format("0x%02X", payload[0])} : ${payload[0].toInt()}")
            }
            52.toByte() -> {
                intent.action = ACTION_VOICE_ASSISTANT_COUNTER
                intent.putExtra(EXTRA_DATA, payload[0].toInt())
                Log.d(TAG, "Voice assistant counter : ${String.format("0x%02X", payload[0])} : ${payload[0].toInt()}")
            }
            54.toByte() -> {
                val lang = payload[0].toInt()
                intent.action = ACTION_CHECK_LANGUAGE
                intent.putExtra(EXTRA_DATA, lang)
                Log.d(TAG, "ORII language is : ${String.format("0x%02X", payload[0])} : $lang")
            }
            55.toByte() -> {
                Log.d(TAG, "ORII language is already : ${String.format("0x%02X", payload[0])} : ${payload[0].toInt()}")
            }
            32.toByte() -> {
                intent.action = ACTION_SINGLE_BUTTON_PRESSED
                Log.d(TAG, "Single button pressed")
            }
            33.toByte() -> {
                intent.action = ACTION_SINGLE_BUTTON_LONG_PRESSED
                Log.d(TAG, "Single button long pressed")
            }
            34.toByte() -> {
                intent.action = ACTION_SINGLE_BUTTON_DOUBLE_PRESSED
                intent.putExtra(EXTRA_DATA, payload[0].toInt())
                Log.d(TAG, "Single button double pressed")
            }
            35.toByte() -> {
                intent.action = ACTION_DOUBLE_BUTTON_PRESSED
                Log.d(TAG, "Double button pressed")
            }
            57.toByte() -> {
                val gestureMode = payload[0].toInt()
                intent.action = ACTION_CHECK_GESTURE_MODE
                intent.putExtra(EXTRA_DATA, gestureMode)
                Log.d(TAG, "ORII gesture mode: ${String.format("0x%02X", payload[0])} : $gestureMode")
            }
            58.toByte() -> {
                val sensitivity = payload[0].toInt()
                intent.action = ACTION_CHECK_SENSITIVITY_OF_GESTURE
                intent.putExtra(EXTRA_DATA, sensitivity)
                Log.d(TAG, "ORII sensitivity of gesture: ${String.format("0x%02X", payload[0])} : $sensitivity")
            }
            59.toByte() -> {
                when (payload[0].toInt()) {
                    0 -> {
                        intent.action = ACTION_GESTURE_FLAT_TRIPLE_TAP
                        Log.d(TAG, "Flat triple tap trigger ORII gesture")
                    }
                    1 -> {
                        intent.action = ACTION_GESTURE_REVERSE_DOUBLE_TAP
                        Log.d(TAG, "Reverse double tap trigger ORII gesture")
                    }
                    else -> {
                        Log.d(TAG, "Undefined gesture command payload: ${String.format("0x%02X", payload[0])} : ${payload[0].toInt()}")
                    }
                }
            }
            else -> {
                Log.d(TAG, "ORII undefined command: ${String.format("0x%02X", payload[0])} : ${payload[0].toInt()}")
            }
        }
        callbacks.forEach { it.onDataReceived(intent) }
    }

    // Fonctions pour envoyer des tâches dans les channels

    private fun putFixedTask(task: () -> Unit) {
        taskScope.launch {
            fixedTaskChannel.send(task)
        }
    }

    private fun putUnfixedTask(task: () -> Unit) {
        taskScope.launch {
            unfixedTaskChannel.send(task)
        }
    }

    // Méthodes existantes de planification de tâches (adaptées aux channels)
    fun putCallCheckTestModeTask() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand((-108).toByte(), 0, null)
            }
        }
    }

    fun putCallCheckMicModeTask() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(48.toByte(), 0, null)
            }
        }
    }

    fun putCallVoiceAssistantCounterTask() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(52.toByte(), 0, null)
            }
        }
    }

    fun putCallCheckLanguageTask() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(54.toByte(), 0, null)
            }
        }
    }

    fun putCallBatteryLevelTaskToFixedChannel() {
        putFixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(7.toByte(), 0, null)
            }
        }
    }

    fun putCallBatteryLevelTaskToUnfixedChannel() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(7.toByte(), 0, null)
            }
        }
    }

    fun putCallFirmwareVersionTaskToFixedChannel() {
        putFixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(8.toByte(), 0, null)
            }
        }
    }

    fun putCallFirmwareVersionTaskToUnfixedChannel() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(8.toByte(), 0, null)
            }
        }
    }

    fun putCallSwitchTestModeTask() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand((-112).toByte(), 0, null)
            }
        }
    }

    // Méthodes ajoutées pour répliquer les fonctionnalités de la version Java

    fun putCallSwitchMicModeTask(micMode: Int) {
        putUnfixedTask {
            val b: Byte = if (micMode == 0) 0 else 1
            if (connectionManager.isOriiConnected()) {
                sendCommand(Framer.STDOUT_FRAME_PREFIX.toByte(), 1, byteArrayOf(b))
            }
        }
    }

    fun putCallChangeLanguageTask(langId: Int) {
        putUnfixedTask {
            val b: Byte = if (langId == 0) 0 else 1
            if (connectionManager.isOriiConnected()) {
                sendCommand(55.toByte(), 1, byteArrayOf(b))
            }
        }
    }

    fun putCallAllowLinePhonecallPickUpTask() {
        putUnfixedTask {
            if (connectionManager.isOriiConnected()) {
                sendCommand(56.toByte(), 0, null)
            }
        }
    }

    fun putCallChangeGestureModeTask(gestureMode: Int) {
        putUnfixedTask {
            val b: Byte = gestureMode.toByte()
            if (connectionManager.isOriiConnected()) {
                sendCommand(57.toByte(), 1, byteArrayOf(b))
            }
        }
    }

    fun putCallChangeSensitivityOfGestureTask(sensitivity: Int) {
        putUnfixedTask {
            val b: Byte = sensitivity.toByte()
            if (connectionManager.isOriiConnected()) {
                sendCommand(58.toByte(), 1, byteArrayOf(b))
            }
        }
    }

    fun putCallMessageReceivedTask(
        ledColor: Int,
        vibration: Int,
        secondaryLedColor: Int,
        secondaryVibration: Int,
        windowing: Boolean
    ) {
        if (connectionManager.isOriiConnected()) {
            if (this.windowing && windowing) return
            // Démarrage d'un job pour gérer la période "windowing"
            taskScope.launch {
                this@CommandManager.windowing = true
                delay(5000L)
                this@CommandManager.windowing = false
            }
            putUnfixedTask {
                sendCommand(3.toByte(), 2, byteArrayOf(getColorCode(ledColor), getVibrationCode(vibration)))
            }
            putUnfixedTask {
                sendCommand(3.toByte(), 2, byteArrayOf(getColorCode(secondaryLedColor), getVibrationCode(secondaryVibration)))
            }
        }
    }
}
