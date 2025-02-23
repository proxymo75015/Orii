package com.origamilabs.orii.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.JsonObject
import com.origamilabs.orii.API
import com.origamilabs.orii.Constants
import com.origamilabs.orii.R
import com.origamilabs.orii.analytics.AnalyticsManager
import com.origamilabs.orii.controller.DeviceController
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.database.AppManager
import com.origamilabs.orii.models.FirmwareVersionInfo
import com.origamilabs.orii.models.User
import com.origamilabs.orii.models.VoiceAssistantCounter
import com.origamilabs.orii.models.`enum`.CustomCommandAction
import com.origamilabs.orii.notification.OriiNotificationListenerService
import com.origamilabs.orii.notification.listeners.OnIncomingCallReceivedListener
import com.origamilabs.orii.notification.listeners.OnNotificationReceivedListener
import com.origamilabs.orii.notification.listeners.OnSmsReceivedListener
import com.origamilabs.orii.notification.receivers.IncomingCallReceiver
import com.origamilabs.orii.notification.receivers.NotificationReceiver
import com.origamilabs.orii.notification.receivers.PhoneCallReceiver
import com.origamilabs.orii.notification.receivers.SmsReceiver
import com.origamilabs.orii.utils.CalendarContentResolver
import com.origamilabs.orii.utils.DeviceLocale
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class AppService : Service(), OnSmsReceivedListener, OnNotificationReceivedListener, OnIncomingCallReceivedListener {

    companion object {
        const val TAG = "AppService"
    }

    private var deviceController: DeviceController? = null
    private var incomingCallReceiver: IncomingCallReceiver? = null
    private var mIncomingCallHandler: IncomingCallHandler? = null
    private var mMessageHandler: MessageHandler? = null
    private var notificationReceiver: NotificationReceiver? = null
    private var phoneCallReceiver: PhoneCallReceiver? = null
    private var smsReceiver: SmsReceiver? = null
    private var tts: TextToSpeech? = null

    private var showingLowBatteryNotification = false

    private val eventListeners: ArrayList<AppServiceListener> = arrayListOf()

    private var firmwareDownloadId: Long = -1L
    private var appVersionInfoDownloadId: Long = -1L

    // Broadcast receiver pour le téléchargement firmware normal
    private val downloadFirmwareCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra("extra_download_id", -1L)
            if (downloadId != null && downloadId == firmwareDownloadId) {
                Log.d(TAG, "DownloadManager Completed")
                unregisterReceiver(this)
                sendBroadcast(Intent(Constants.FIRMWARE_DOWNLOADED_BROADCAST))
                AppManager.instance.setCanFirmwareUpdate(true)
                firmwareDownloadId = -1L
            }
        }
    }

    // Broadcast receiver pour le téléchargement firmware forcé
    private val forceDownloadFirmwareCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra("extra_download_id", -1L)
            if (downloadId != null && downloadId == firmwareDownloadId) {
                Log.d(TAG, "DownloadManager Completed")
                unregisterReceiver(this)
                sendBroadcast(Intent(Constants.FIRMWARE_FORCE_DOWNLOADED_BROADCAST))
                AppManager.instance.setCanFirmwareForceUpdate(true)
                firmwareDownloadId = -1L
            }
        }
    }

    // Broadcast receiver pour le téléchargement d'informations de version d'application
    private val downloadAppVersionCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra("extra_download_id", -1L)
            if (downloadId != null && downloadId == appVersionInfoDownloadId) {
                Log.d(TAG, "DownloadManager Completed")
                unregisterReceiver(this)
                sendBroadcast(Intent(Constants.APP_VERSION_INFO_DOWNLOADED_BROADCAST))
                appVersionInfoDownloadId = -1L
            }
        }
    }

    // Binder pour la liaison du service
    private val binder = LocalBinder()

    // Callback de connexion Bluetooth
    private val connectionCallback = object : ConnectionManager.Callback {
        override fun onA2dpStateChange(p0: Int, p1: Int) {}
        override fun onGattStateChange(p0: Int, p1: Int) {}
        override fun onHeadsetStateChange(p0: Int, p1: Int) {}
        override fun onOriiRemoveBond() {}
        override fun onOriiStateChange(prevState: Int, newState: Int) {
            if (newState == 0) {
                removeLowBatteryNotification(this@AppService)
            }
        }
    }

    // Callback pour les commandes Bluetooth
    private val commandCallback = object : CommandManager.Callback {
        override fun onDataReceived(intent: Intent) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    CommandManager.ACTION_CHECK_MIC_MODE ->
                        AppManager.instance.sharedPreferences.setMicMode(intent.getIntExtra(CommandManager.EXTRA_DATA, -1))
                    CommandManager.ACTION_CHECK_LANGUAGE -> {
                        Log.d(TAG, "ORII language is :${intent.getIntExtra(CommandManager.EXTRA_DATA, -1)}")
                        tts?.let { setTTSLanguage(it) }
                    }
                    CommandManager.ACTION_VOICE_ASSISTANT_COUNTER ->
                        insertVaTriggerCount(intent.getIntExtra(CommandManager.EXTRA_DATA, -1))
                    CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP ->
                        triggerCustomCommandAction(CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP)
                    CommandManager.ACTION_CHECK_SENSITIVITY_OF_GESTURE ->
                        AppManager.instance.sharedPreferences.setSensitivityOfGesture(intent.getIntExtra(CommandManager.EXTRA_DATA, 0))
                    CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP ->
                        triggerCustomCommandAction(CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP)
                    CommandManager.ACTION_SINGLE_BUTTON_DOUBLE_PRESSED ->
                        readOutMessage()
                    CommandManager.ACTION_BATTERY_LEVEL -> {
                        val level = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                        AppManager.instance.setBatteryLevel(level)
                        updateOriiBatteryLevel(level)
                    }
                    CommandManager.ACTION_GESTURE_SIDE_DOUBLE_TAP ->
                        deviceController?.callMediaPlayOrPause()
                    CommandManager.ACTION_VOICE_ASSISTANT_STATE_CHANGED ->
                        insertVaTriggerCount(if (intent.getIntExtra(CommandManager.EXTRA_DATA, -1) == 0) 1 else 0)
                    CommandManager.ACTION_FIRMWARE_VERSION -> {
                        val firmwareVersion = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                        AppManager.instance.setFirmwareVersion(firmwareVersion)
                        checkFirmwareVersion(firmwareVersion)
                    }
                    CommandManager.ACTION_CHECK_GESTURE_MODE ->
                        AppManager.instance.sharedPreferences.setGestureMode(intent.getIntExtra(CommandManager.EXTRA_DATA, 0))
                    CommandManager.ACTION_MIC_MODE_CHANGED ->
                        AppManager.instance.sharedPreferences.setMicMode(intent.getIntExtra(CommandManager.EXTRA_DATA, -1))
                }
            }
            // Notifier les listeners d'événements
            for (listener in eventListeners) {
                listener.onDataReceived(intent)
            }
        }
    }

    // Interface pour les écouteurs d'événements du service
    interface AppServiceListener {
        fun onDataReceived(intent: Intent)
    }

    fun addListener(listener: AppServiceListener): Boolean {
        return eventListeners.add(listener)
    }

    fun removeListener(listener: AppServiceListener): Boolean {
        return eventListeners.remove(listener)
    }

    override fun onCreate() {
        super.onCreate()

        // Vérifier que le NotificationListenerService est actif
        isNotificationListenerServiceRunning(this)

        mMessageHandler = MessageHandler()

        smsReceiver = SmsReceiver().also {
            registerReceiver(it, IntentFilter(Constants.ACTION_SMS_RECEIVED))
            it.setOnSmsReceivedListener(this)
        }

        notificationReceiver = NotificationReceiver().also {
            registerReceiver(it, IntentFilter(Constants.ACTION_NOTIFICATION_RECEIVED))
            it.setOnNotificationReceivedListener(this)
        }

        incomingCallReceiver = IncomingCallReceiver().also {
            registerReceiver(it, IntentFilter(Constants.ACTION_INCOMING_CALL_RECEIVED))
            it.setOnIncomingCallReceivedListener(this)
        }

        phoneCallReceiver = PhoneCallReceiver().also {
            registerReceiver(it, IntentFilter("android.intent.action.PHONE_STATE"))
        }

        deviceController = DeviceController(this)

        setupTTS()
        initCheckAppVersionCounterTimer()

        ConnectionManager.getInstance().addCallback(connectionCallback)
        CommandManager.getInstance().addCallback(commandCallback)
    }

    fun updateOriiBatteryLevel(batteryLevel: Int) {
        if (batteryLevel <= 1) {
            showLowBatteryNotification(this)
        } else {
            removeLowBatteryNotification(this)
        }
    }

    private fun showLowBatteryNotification(context: Context) {
        if (showingLowBatteryNotification) return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.notification_battery_channel_id)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_statusbar)
            .setContentTitle(context.getString(R.string.notification_title_battery_low))
            .setContentText(context.getString(R.string.notification_text_battery_low))
            .setAutoCancel(true)
            .setOngoing(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, context.getString(R.string.notification_connection_channel_name), NotificationManager.IMPORTANCE_HIGH)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        notificationManager.notify(com.origamilabs.orii.core.Constants.ORII_NOTIFICATION_LOW_BATTERY, builder.build())
        showingLowBatteryNotification = true
    }

    fun removeLowBatteryNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(com.origamilabs.orii.core.Constants.ORII_NOTIFICATION_LOW_BATTERY)
        showingLowBatteryNotification = false
    }

    fun checkFirmwareVersion(version: Int) {
        if (AppManager.instance.firmwareVersionChecked || AppManager.instance.canFirmwareUpdate || version == -1) return
        API.instance.checkFirmwareVersion(version, AppManager.instance.currentUser!!.token, object : API.ResponseListener {
            override fun onError(errorMessage: String) {
                // Gérer l'erreur si nécessaire
            }

            override fun onSuccess(response: JsonObject) {
                AppManager.instance.firmwareVersionChecked = true
                if (response.get("need_update").asBoolean) {
                    val versionObj = response.getAsJsonObject("version")
                    val versionNumber = versionObj.get("version_number").asInt
                    val url = versionObj.get("version_file_url").asString
                    val remark = versionObj.get("version_remark").asString
                    val bugFixes = versionObj.get("version_bug_fixes").asString
                    val newFeatures = versionObj.get("version_new_features").asString
                    val firmwareVersionInfo = FirmwareVersionInfo(versionNumber, url, remark, bugFixes, newFeatures)
                    AppManager.instance.firmwareVersionInfo = firmwareVersionInfo
                    downloadFirmware(firmwareVersionInfo)
                } else {
                    AppManager.instance.setCanFirmwareUpdate(false)
                }
            }
        })
    }

    fun forceUpdateFirmwareVersion(version: Int) {
        val firmwareVersionInfo = when (version) {
            68 -> FirmwareVersionInfo(68, "https://orii.s3.amazonaws.com/firmware/android/80416a5d687538c254bf09651b2848c11557811642215V68_OTA.bin", "V68, full OTA image", "V68", "V68")
            69 -> FirmwareVersionInfo(69, "https://orii.s3.amazonaws.com/firmware/android/608a15184d9757c1c2ad578c38600cf51563336461590V69_OTA.bin", "Gesture-control features", "V69", "V69")
            70 -> FirmwareVersionInfo(70, "https://orii.s3.ap-southeast-1.amazonaws.com/firmware/android/a242027694e72629907771be0193d3051565944307099V70_OTA.bin", "Gesture Phase II", "V70", "V70")
            71 -> FirmwareVersionInfo(71, "https://orii.s3.ap-southeast-1.amazonaws.com/firmware/android/199e446190e6aea46087815ddef266171573098477517V71_OTA.bin", "Dynamic Audio Revamp", "V71", "V71")
            else -> FirmwareVersionInfo(71, "https://orii.s3.ap-southeast-1.amazonaws.com/firmware/android/199e446190e6aea46087815ddef266171573098477517V71_OTA.bin", "Dynamic Audio Revamp", "Clearer audio interactions in loud environments.", "")
        }
        AppManager.instance.firmwareVersionInfo = firmwareVersionInfo
        forceDownloadFirmware(firmwareVersionInfo)
    }

    fun downloadFirmware(versionInfo: FirmwareVersionInfo) {
        val externalFilesDir = getExternalFilesDir("update")
        val file = File(externalFilesDir?.absolutePath, "v${versionInfo.versionNumber}.bin")
        Log.d(TAG, "file.exists():${file.exists()},  absolutePath:${file.absolutePath}")
        if (file.absoluteFile.exists() && firmwareDownloadId == -1L) {
            AppManager.instance.setCanFirmwareUpdate(true)
            sendBroadcast(Intent(Constants.FIRMWARE_DOWNLOADED_BROADCAST))
        } else if (firmwareDownloadId == -1L) {
            registerReceiver(downloadFirmwareCompleteBroadcastReceiver, IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"))
            firmwareDownloadId = API.instance.downloadFirmware(this, versionInfo.versionNumber, versionInfo.url, "Downloading firmware...")
        }
    }

    private fun forceDownloadFirmware(versionInfo: FirmwareVersionInfo) {
        val externalFilesDir = getExternalFilesDir("update")
        val file = File(externalFilesDir?.absolutePath, "v${versionInfo.versionNumber}.bin")
        Log.d(TAG, "file.exists():${file.exists()},  absolutePath:${file.absolutePath}")
        if (file.absoluteFile.exists() && firmwareDownloadId == -1L) {
            AppManager.instance.setCanFirmwareForceUpdate(true)
            sendBroadcast(Intent(Constants.FIRMWARE_FORCE_DOWNLOADED_BROADCAST))
        } else if (firmwareDownloadId == -1L) {
            registerReceiver(forceDownloadFirmwareCompleteBroadcastReceiver, IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"))
            firmwareDownloadId = API.instance.downloadFirmware(this, versionInfo.versionNumber, versionInfo.url, "Downloading firmware...")
        }
    }

    fun triggerCustomCommandAction(triggerGesture: String) {
        var customCommandAction = CustomCommandAction.WEB_HOOK
        if (triggerGesture == CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP) {
            customCommandAction = AppManager.instance.sharedPreferences.flatTripleTapAction
        } else if (triggerGesture == CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP) {
            customCommandAction = AppManager.instance.sharedPreferences.reverseDoubleTapAction
        }
        when (customCommandAction) {
            CustomCommandAction.WEB_HOOK -> API.instance.callWebHookTriggerUri(AppManager.instance.sharedPreferences.flatTripleTapWebHookUrl)
            CustomCommandAction.DO_NOT_DISTURB_MODE -> deviceController?.switchDisturbMode()
            CustomCommandAction.FLASHLIGHT_SWITCH -> deviceController?.switchFlashlight()
            CustomCommandAction.SCREEN_ON_OFF -> deviceController?.switchScreenLock()
            CustomCommandAction.TIME_READOUT -> speakCurrentTime(false)
            CustomCommandAction.CALENDAR_READOUT -> speakCalendarEvent()
        }
    }

    private fun speakCurrentTime(isShort: Boolean) {
        val simpleDateFormat = when {
            isShort -> SimpleDateFormat("h:mm a", DeviceLocale.instance.deviceLocale)
            DeviceLocale.instance.deviceLocale.language.equals("zh", ignoreCase = true) ->
                SimpleDateFormat("MMMMdd日EEEE, HH:mm", DeviceLocale.instance.deviceLocale)
            else -> SimpleDateFormat("EEEE, dd, MMMM, HH:mm", DeviceLocale.instance.deviceLocale)
        }
        val calendar = Calendar.getInstance()
        val currentDateTime = simpleDateFormat.format(calendar.time)
        readOutMessage(currentDateTime)
    }

    private fun speakCalendarEvent() {
        val todayEvents = CalendarContentResolver.instance.getTodayEvent(this)
        val sb = StringBuilder()
        for (event in todayEvents) {
            if (event.allDay) {
                sb.append(getString(R.string.tts_message_all_day))
            } else {
                sb.append(event.beginTime)
            }
            sb.append(" ${event.event}\n")
        }
        readOutMessage(sb.toString())
    }

    fun insertVaTriggerCount(times: Int) {
        if (times == 0) return
        AppManager.instance.runQueryOnBackground {
            AppManager.instance.database.vaCounterDao().insert(
                VoiceAssistantCounter(times, (System.currentTimeMillis() / 1000).toInt())
            )
        }
    }

    private fun initCheckAppVersionCounterTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                registerReceiver(downloadAppVersionCompleteBroadcastReceiver, IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"))
                appVersionInfoDownloadId = API.instance.downloadAppVersionInformation(
                    this@AppService,
                    Constants.PUBLIC_APP_VERSION_INFORMATION_URL,
                    "Downloading app version information..."
                )
            }
        }, 0L, 43200000L)
    }

    private fun isNotificationListenerServiceRunning(context: Context): Boolean {
        val componentName = ComponentName(context, OriiNotificationListenerService::class.java)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        var isRunning = false
        for (serviceInfo in runningServices) {
            if (serviceInfo.service == componentName && serviceInfo.pid == Process.myPid()) {
                isRunning = true
            }
        }
        if (!isRunning) {
            toggleNotificationListenerService(context)
        }
        return isRunning
    }

    private fun toggleNotificationListenerService(context: Context) {
        val packageManager = packageManager
        packageManager.setComponentEnabledSetting(
            ComponentName(context, OriiNotificationListenerService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            ComponentName(context, OriiNotificationListenerService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    // OnSmsReceivedListener
    override fun onSmsReceived(sender: String, message: String) {
        Log.d(TAG, "onSmsReceived")
        mMessageHandler?.addMessage("sms", sender, message)
        AppManager.instance.runQueryOnBackground {
            val app = AppManager.instance.database.applicationDao().findByPackageName("sms")
            var personApp = AppManager.instance.database.personDao().findByPersonName(sender)
            if (app != null) {
                if (personApp == null) {
                    personApp = com.origamilabs.orii.models.Application(-1, 0, 0, "")
                }
                CommandManager.getInstance().putCallMessageReceivedTask(
                    app.ledColor, app.vibration, personApp.ledColor, personApp.vibration, true
                )
            }
        }
    }

    // OnNotificationReceivedListener
    override fun onNotificationReceived(packageName: String, sender: String, message: String) {
        Log.d(TAG, "onNotificationReceived")
        mMessageHandler?.addMessage(packageName, sender, message)
        AppManager.instance.runQueryOnBackground {
            val app = AppManager.instance.database.applicationDao().findByPackageName(packageName)
            var personApp = AppManager.instance.database.personDao().findByPersonName(sender)
            if (app != null) {
                if (personApp == null) {
                    personApp = com.origamilabs.orii.models.Application(-1, 0, 0, "")
                }
                CommandManager.getInstance().putCallMessageReceivedTask(
                    app.ledColor, app.vibration, personApp.ledColor, personApp.vibration, true
                )
            }
        }
    }

    // OnIncomingCallReceivedListener
    override fun onIncomingCallReceived(packageName: String) {
        Log.d(TAG, "onIncomingCallReceived and packageName is: $packageName")
        CommandManager.getInstance().putCallAllowLinePhonecallPickUpTask()
        AnalyticsManager.instance.logVoiceCall(packageName)
    }

    private fun setupTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    setTTSLanguage(it)
                }
                mIncomingCallHandler = IncomingCallHandler()
                phoneCallReceiver?.addListener(mIncomingCallHandler!!)
            }
        }
    }

    fun setTTSLanguage(tts: TextToSpeech) {
        val deviceLocale = DeviceLocale.instance.deviceLocale
        if (deviceLocale.language.equals("ja", ignoreCase = true)) {
            CommandManager.getInstance().putCallChangeLanguageTask(1)
        } else {
            CommandManager.getInstance().putCallChangeLanguageTask(0)
        }
        tts.language = deviceLocale
    }

    fun readOutMessage() {
        val ttsInstance = tts ?: throw UninitializedPropertyAccessException("tts")
        if (ttsInstance.isSpeaking) {
            ttsInstance.stop()
            val cleared = getString(R.string.tts_message_cleared)
            readOutMessage(cleared)
            return
        }
        val speechQueue = mMessageHandler?.getSpeechQueue(this) ?: arrayListOf()
        if (speechQueue.isNotEmpty()) {
            speakCurrentTime(true)
            for (msg in speechQueue) {
                readOutMessage(msg)
            }
        } else {
            val noNew = getString(R.string.tts_no_new_messages)
            readOutMessage(noNew)
        }
    }

    private fun readOutMessage(speech: String) {
        val ttsInstance = tts ?: throw UninitializedPropertyAccessException("tts")
        setTTSLanguage(ttsInstance)
        Log.d(TAG, "TTS read out: $speech")
        ttsInstance.setSpeechRate(AppManager.instance.sharedPreferences.readoutSpeed)
        ttsInstance.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): AppService = this@AppService
    }
}
