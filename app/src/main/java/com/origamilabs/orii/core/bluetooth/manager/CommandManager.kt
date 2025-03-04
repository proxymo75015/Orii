@file:OptIn(ExperimentalCoroutinesApi::class)

package com.origamilabs.orii.core.bluetooth.manager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.content.Intent
import com.origamilabs.orii.core.bluetooth.BluetoothService
import com.origamilabs.orii.core.bluetooth.connection.GattHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CommandManager @Inject constructor(
    @ApplicationContext context: Context,
    bluetoothManager: android.bluetooth.BluetoothManager,
    bluetoothAdapter: BluetoothAdapter,
    bluetoothService: BluetoothService,
    private val connectionManager: ConnectionManager
) : BaseManager(context, bluetoothManager, bluetoothAdapter, bluetoothService) {

    companion object {
        private const val TAG = "CommandManager"

        // Actions destinées à l'interface utilisateur (ces constantes servent à déclencher des actions via des Intent)
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

        // UUID et constantes techniques
        private val ORII_PROFILE_UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        private val ORII_NOTIFY_1_UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")
        private val ORII_WRITE_UUID = UUID.fromString("0000FFF6-0000-1000-8000-00805F9B34FB")
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        // Remplacement de Framer.STDOUT_FRAME_PREFIX par une constante locale
        private const val CUSTOM_STDOUT_FRAME_PREFIX: Byte = 0x10

        // Codes de commande
        private const val CMD_BATTERY_LEVEL: Byte = 7
        private const val CMD_FIRMWARE_VERSION: Byte = 8
        private const val CMD_CHECK_MIC_MODE: Byte = 48
        private const val CMD_SWITCH_TEST_MODE: Byte = -112
        private const val CMD_CHANGE_LANGUAGE: Byte = 55
        private const val CMD_ALLOW_LINE_PHONECALL_PICKUP: Byte = 56
        private const val CMD_CHANGE_GESTURE_MODE: Byte = 57
        private const val CMD_CHANGE_SENSITIVITY: Byte = 58
        private const val CMD_MESSAGE_RECEIVED: Byte = 3

        // En-tête et pied de paquet
        private const val HEADER: Byte = -86
        private const val END: Byte = -1

        // Intervalles de temps
        private const val FIXED_TASK_INTERVAL = 30000L
        private const val UNFIXED_TASK_INTERVAL = 1000L
        private const val INITIAL_DELAY = 1000L
        private const val WINDOWING_DELAY = 5000L
    }

    private var batteryLevel: Int = 0
    private var firmwareVersion: Int = 0
    private var bluetoothGatt: BluetoothGatt? = null
    private var gattHandler: GattHandler? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var isWritingCharacteristic = false

    private val fixedTaskChannel = Channel<() -> Unit>(Channel.UNLIMITED)
    private val unfixedTaskChannel = Channel<() -> Unit>(Channel.UNLIMITED)
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
            if (newState == ConnectionManager.STATE_CONNECTED) {
                scheduleCheckLanguageTask()
                scheduleBatteryLevelTaskUnfixed()
                scheduleFirmwareVersionTaskUnfixed()
                scheduleVoiceAssistantCounterTask()
                scheduleCheckMicModeTask()
                scheduleSwitchTestModeTask()
            }
        }
    }

    private var windowing = false

    override fun onInitialize(): Boolean {
        // Initialisation spécifique si nécessaire
        return true
    }

    override fun start() {
        // La méthode start() par défaut n'est pas utilisée.
    }

    @SuppressLint("MissingPermission")
    fun start(gattHandler: GattHandler, bluetoothGatt: BluetoothGatt) {
        this.gattHandler = gattHandler
        this.bluetoothGatt = bluetoothGatt
        connectionManager.addCallback(connectionCallback)
        val service = bluetoothGatt.getService(ORII_PROFILE_UUID)
        if (service == null) {
            Timber.d("Service de profil non trouvé")
            gattHandler.disconnect()
            return
        }
        writeCharacteristic = service.getCharacteristic(ORII_WRITE_UUID)
        notifyCharacteristic = service.getCharacteristic(ORII_NOTIFY_1_UUID)
        setCharacteristicNotification(notifyCharacteristic, true)
        startTaskSchedulers()
    }

    private fun startTaskSchedulers() {
        fixedTaskJob?.cancel()
        unfixedTaskJob?.cancel()
        fixedTaskJob = taskScope.launch {
            delay(INITIAL_DELAY)
            while (isActive) {
                if (fixedTaskChannel.isEmpty) {
                    scheduleBatteryLevelTaskFixed()
                    scheduleFirmwareVersionTaskFixed()
                }
                if (!isWritingCharacteristic) {
                    fixedTaskChannel.receive().invoke()
                }
                Timber.d("Tâche fixe envoyée")
                delay(FIXED_TASK_INTERVAL)
            }
        }
        unfixedTaskJob = taskScope.launch {
            delay(INITIAL_DELAY)
            while (isActive) {
                if (!isWritingCharacteristic && !unfixedTaskChannel.isEmpty) {
                    unfixedTaskChannel.receive().invoke()
                }
                Timber.d("Tâche variable envoyée")
                delay(UNFIXED_TASK_INTERVAL)
            }
        }
    }

    override fun onClose() {
        Timber.d("Fermeture de CommandManager")
        batteryLevel = -1
        firmwareVersion = -1
        gattHandler = null
        connectionManager.removeCallback(connectionCallback)
        fixedTaskJob?.cancel()
        unfixedTaskJob?.cancel()
        taskScope.cancel()
        fixedTaskChannel.cancel()
        unfixedTaskChannel.cancel()
    }

    fun addCallback(callback: Callback) {
        if (!callbacks.contains(callback)) callbacks.add(callback)
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    fun getBatteryLevel() = batteryLevel
    fun getFirmwareVersion() = firmwareVersion

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun writeCharacteristic(bytes: ByteArray): Boolean {
        isWritingCharacteristic = true
        return if (bluetoothGatt != null) {
            val hexString = bytes.joinToString(" ") { String.format("0x%02X", it) }
            Timber.d("Écriture de la caractéristique : $hexString")
            writeCharacteristic?.setValue(bytes)
            val result = bluetoothGatt!!.writeCharacteristic(writeCharacteristic)
            Timber.d("Résultat de l'écriture : $result")
            isWritingCharacteristic = false
            result
        } else {
            Timber.w("BluetoothGatt non initialisé")
            isWritingCharacteristic = false
            false
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic?, enabled: Boolean) {
        if (bluetoothGatt == null) {
            Timber.w("BluetoothGatt non initialisé")
            return
        }
        bluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)?.apply {
            value = if (enabled)
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            else
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            bluetoothGatt!!.writeDescriptor(this)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendCommand(cmd: Byte, length: Byte, data: ByteArray?): Boolean {
        val packetLength = length.toInt() + 4
        val packet = ByteArray(packetLength).apply {
            this[0] = HEADER
            this[1] = cmd
            this[2] = length
            this[this.lastIndex] = END
            data?.let { System.arraycopy(it, 0, this, 3, length.toInt()) }
        }
        return writeCharacteristic(packet)
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun handleNotification(characteristic: BluetoothGattCharacteristic) {
        @Suppress("DEPRECATION")
        val value = characteristic.value
        if (value.size <= 4) return
        val header = value[0]
        val cmd = value[1]
        val len = value[2]
        val payload = value.copyOfRange(3, len.toInt() + 3)
        val end = value[len.toInt() + 3]
        val hexString = value.joinToString(" ") { String.format("%02X", it) }
        Timber.d("Données reçues de ${characteristic.uuid} : $hexString")
        if (header != HEADER || end != END) {
            Timber.e("Format de paquet invalide")
        } else {
            processCommand(cmd, payload)
        }
    }

    private fun processCommand(cmd: Byte, payload: ByteArray) {
        val intent = Intent()
        when (cmd) {
            (-112).toByte() -> {
                Timber.d("Mode test modifié : ${payload.firstOrNull()?.let { String.format("0x%02X", it) }}")
            }
            (-108).toByte() -> {
                val testMode = payload.firstOrNull()?.toInt() ?: 0
                Timber.d("Mode test : $testMode")
                if (testMode == 2) scheduleSwitchTestModeTask()
            }
            64.toByte() -> {
                intent.action = ACTION_GESTURE_SIDE_DOUBLE_TAP
                Timber.d("Geste : double tap latéral")
            }
            CMD_BATTERY_LEVEL -> {
                val level = payload.firstOrNull()?.toInt() ?: 0
                batteryLevel = level
                intent.action = ACTION_BATTERY_LEVEL
                intent.putExtra(EXTRA_DATA, level)
                Timber.d("Niveau de batterie : $level")
            }
            CMD_FIRMWARE_VERSION -> {
                val fw = payload.firstOrNull()?.toInt() ?: 0
                firmwareVersion = fw
                intent.action = ACTION_FIRMWARE_VERSION
                intent.putExtra(EXTRA_DATA, fw)
                Timber.d("Version firmware : $fw")
            }
            CMD_CHECK_MIC_MODE -> {
                val micMode = payload.firstOrNull()?.toInt() ?: 0
                intent.action = ACTION_CHECK_MIC_MODE
                intent.putExtra(EXTRA_DATA, micMode)
                Timber.d("Mode micro : $micMode")
            }
            49.toByte() -> {
                val newMicMode = payload.firstOrNull()?.toInt() ?: 0
                intent.action = ACTION_MIC_MODE_CHANGED
                intent.putExtra(EXTRA_DATA, newMicMode)
                Timber.d("Changement de mode micro : $newMicMode")
            }
            51.toByte() -> {
                intent.action = ACTION_VOICE_ASSISTANT_STATE_CHANGED
                intent.putExtra(EXTRA_DATA, payload.firstOrNull()?.toInt() ?: 0)
                Timber.d("Changement d'état de l'assistant vocal")
            }
            52.toByte() -> {
                intent.action = ACTION_VOICE_ASSISTANT_COUNTER
                intent.putExtra(EXTRA_DATA, payload.firstOrNull()?.toInt() ?: 0)
                Timber.d("Compteur de l'assistant vocal")
            }
            54.toByte() -> {
                val lang = payload.firstOrNull()?.toInt() ?: 0
                intent.action = ACTION_CHECK_LANGUAGE
                intent.putExtra(EXTRA_DATA, lang)
                Timber.d("Vérification de la langue : $lang")
            }
            55.toByte() -> {
                Timber.d("Langue inchangée : ${payload.firstOrNull()?.toInt()}")
            }
            32.toByte() -> {
                intent.action = ACTION_SINGLE_BUTTON_PRESSED
                Timber.d("Bouton unique pressé")
            }
            33.toByte() -> {
                intent.action = ACTION_SINGLE_BUTTON_LONG_PRESSED
                Timber.d("Bouton unique maintenu")
            }
            34.toByte() -> {
                intent.action = ACTION_SINGLE_BUTTON_DOUBLE_PRESSED
                intent.putExtra(EXTRA_DATA, payload.firstOrNull()?.toInt() ?: 0)
                Timber.d("Bouton unique double pressé")
            }
            35.toByte() -> {
                intent.action = ACTION_DOUBLE_BUTTON_PRESSED
                Timber.d("Bouton double pressé")
            }
            57.toByte() -> {
                val gestureMode = payload.firstOrNull()?.toInt() ?: 0
                intent.action = ACTION_CHECK_GESTURE_MODE
                intent.putExtra(EXTRA_DATA, gestureMode)
                Timber.d("Mode gestuel : $gestureMode")
            }
            58.toByte() -> {
                val sensitivity = payload.firstOrNull()?.toInt() ?: 0
                intent.action = ACTION_CHECK_SENSITIVITY_OF_GESTURE
                intent.putExtra(EXTRA_DATA, sensitivity)
                Timber.d("Sensibilité gestuelle : $sensitivity")
            }
            59.toByte() -> {
                when (payload.firstOrNull()?.toInt() ?: -1) {
                    0 -> {
                        intent.action = ACTION_GESTURE_FLAT_TRIPLE_TAP
                        Timber.d("Triple tap plat déclenché")
                    }
                    1 -> {
                        intent.action = ACTION_GESTURE_REVERSE_DOUBLE_TAP
                        Timber.d("Double tap inverse déclenché")
                    }
                    else -> Timber.d("Commande de geste indéfinie")
                }
            }
            else -> Timber.d("Commande indéfinie : ${payload.firstOrNull()?.toInt()}")
        }
        callbacks.forEach { it.onDataReceived(intent) }
    }

    // Exécute la tâche uniquement si la connexion est active.
    private inline fun executeIfConnected(task: () -> Unit) {
        if (connectionManager.isOriiConnected()) task.invoke()
    }

    // Méthodes de planification des tâches
    private fun scheduleFixedTask(task: () -> Unit) {
        taskScope.launch { fixedTaskChannel.send(task) }
    }

    private fun scheduleUnfixedTask(task: () -> Unit) {
        taskScope.launch { unfixedTaskChannel.send(task) }
    }

    fun scheduleCheckLanguageTask() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(54.toByte(), 0, null) } }
    }

    fun scheduleBatteryLevelTaskUnfixed() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(CMD_BATTERY_LEVEL, 0, null) } }
    }

    fun scheduleVoiceAssistantCounterTask() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(52.toByte(), 0, null) } }
    }

    fun scheduleCheckMicModeTask() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(CMD_CHECK_MIC_MODE, 0, null) } }
    }

    private fun scheduleBatteryLevelTaskFixed() {
        scheduleFixedTask { executeIfConnected { sendCommand(CMD_BATTERY_LEVEL, 0, null) } }
    }

    private fun scheduleFirmwareVersionTaskFixed() {
        scheduleFixedTask { executeIfConnected { sendCommand(CMD_FIRMWARE_VERSION, 0, null) } }
    }

    fun scheduleFirmwareVersionTaskUnfixed() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(CMD_FIRMWARE_VERSION, 0, null) } }
    }

    private fun scheduleSwitchTestModeTask() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(CMD_SWITCH_TEST_MODE, 0, null) } }
    }

    // Utilise CUSTOM_STDOUT_FRAME_PREFIX pour switchMicMode.
    fun scheduleSwitchMicModeTask(micMode: Int) {
        scheduleUnfixedTask {
            val b: Byte = if (micMode == 0) 0 else 1
            executeIfConnected { sendCommand(CUSTOM_STDOUT_FRAME_PREFIX, 1, byteArrayOf(b)) }
        }
    }

    fun scheduleChangeLanguageTask(langId: Int) {
        scheduleUnfixedTask {
            val b: Byte = if (langId == 0) 0 else 1
            executeIfConnected { sendCommand(CMD_CHANGE_LANGUAGE, 1, byteArrayOf(b)) }
        }
    }

    fun scheduleAllowLinePhonecallPickUpTask() {
        scheduleUnfixedTask { executeIfConnected { sendCommand(CMD_ALLOW_LINE_PHONECALL_PICKUP, 0, null) } }
    }

    fun scheduleChangeGestureModeTask(gestureMode: Int) {
        scheduleUnfixedTask {
            executeIfConnected { sendCommand(CMD_CHANGE_GESTURE_MODE, 1, byteArrayOf(gestureMode.toByte())) }
        }
    }

    fun scheduleChangeSensitivityTask(sensitivity: Int) {
        scheduleUnfixedTask {
            executeIfConnected { sendCommand(CMD_CHANGE_SENSITIVITY, 1, byteArrayOf(sensitivity.toByte())) }
        }
    }

    fun scheduleMessageReceivedTask(
        ledColor: Int,
        vibration: Int,
        secondaryLedColor: Int,
        secondaryVibration: Int,
        windowing: Boolean
    ) {
        if (connectionManager.isOriiConnected()) {
            if (this.windowing && windowing) return
            taskScope.launch {
                this@CommandManager.windowing = true
                delay(WINDOWING_DELAY)
                this@CommandManager.windowing = false
            }
            scheduleUnfixedTask {
                sendCommand(CMD_MESSAGE_RECEIVED, 2, byteArrayOf(getColorCode(ledColor), getVibrationCode(vibration)))
            }
            scheduleUnfixedTask {
                sendCommand(CMD_MESSAGE_RECEIVED, 2, byteArrayOf(getColorCode(secondaryLedColor), getVibrationCode(secondaryVibration)))
            }
        }
    }

    // Fonctions de conversion internes
    private fun getColorCode(code: Int): Byte {
        val result = when (code) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            else -> 0
        }.toByte()
        Timber.d("Code couleur : ${String.format("0x%02X", result)}")
        return result
    }

    private fun getVibrationCode(code: Int): Byte {
        val result = when (code) {
            0 -> 0
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            else -> 1
        }.toByte()
        Timber.d("Code vibration : ${String.format("0x%02X", result)}")
        return result
    }
}
