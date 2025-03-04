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
import com.origamilabs.orii.Constants
import com.origamilabs.orii.R
import com.origamilabs.orii.controller.DeviceController
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.database.AppManager
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

/**
 * Service principal de l'application ORII, chargé de gérer les opérations locales telles que
 * la communication Bluetooth avec la bague, la gestion des notifications et la synthèse vocale.
 *
 * Les fonctionnalités de mise à jour du firmware et les connexions à des serveurs externes ont été supprimées
 * pour rendre l'application autonome.
 */
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
                    // Suppression de la branche firmware puisque la mise à jour externe est désactivée
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

        ConnectionManager.getInstance().addCallback(connectionCallback)
        CommandManager.getInstance().addCallback(commandCallback)

        // Suppression de l'initialisation de la vérification de version d'application
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
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(com.origamilabs.orii.core.Constants.ORII_NOTIFICATION_LOW_BATTERY, builder.build())
        showingLowBatteryNotification = true
    }

    fun removeLowBatteryNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(com.origamilabs.orii.core.Constants.ORII_NOTIFICATION_LOW_BATTERY)
        showingLowBatteryNotification = false
    }

    /**
     * Exécute l'action associée à un geste personnalisé.
     * Pour l'action WEB_HOOK, l'appel externe est désactivé en mode autonome.
     */
    fun triggerCustomCommandAction(triggerGesture: String) {
        val customCommandAction = when (triggerGesture) {
            CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP -> AppManager.instance.sharedPreferences.flatTripleTapAction
            CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP -> AppManager.instance.sharedPreferences.reverseDoubleTapAction
            else -> CustomCommandAction.WEB_HOOK
        }
        when (customCommandAction) {
            CustomCommandAction.WEB_HOOK -> {
                Log.d(TAG, "WEB_HOOK action est désactivée en mode autonome")
                // Aucun appel externe n'est effectué
            }
            CustomCommandAction.DO_NOT_DISTURB_MODE ->
                deviceController?.switchDisturbMode()
            CustomCommandAction.FLASHLIGHT_SWITCH ->
                deviceController?.switchFlashlight()
            CustomCommandAction.SCREEN_ON_OFF ->
                deviceController?.switchScreenLock()
            CustomCommandAction.TIME_READOUT ->
                speakCurrentTime(false)
            CustomCommandAction.CALENDAR_READOUT ->
                speakCalendarEvent()
        }
    }

    private fun speakCurrentTime(isShort: Boolean) {
        // Pour la France, on utilise explicitement Locale.FRANCE
        val frenchLocale = java.util.Locale.FRANCE
        val simpleDateFormat = when {
            isShort -> SimpleDateFormat("HH:mm", frenchLocale) // Format 24h pour la France
            DeviceLocale.instance.deviceLocale.language.equals("zh", ignoreCase = true) ->
                SimpleDateFormat("MMMMdd日EEEE, HH:mm", DeviceLocale.instance.deviceLocale)
            else -> SimpleDateFormat("EEEE, dd MMMM, HH:mm", frenchLocale)
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

    /**
     * Configure la langue du moteur TTS.
     * Pour la France, la langue est forcée en français et la tâche de changement de langue est notifiée via CommandManager.
     */
    fun setTTSLanguage(tts: TextToSpeech) {
        val deviceLocale = DeviceLocale.instance.deviceLocale
        when {
            deviceLocale.language.equals("ja", ignoreCase = true) -> {
                CommandManager.getInstance().putCallChangeLanguageTask(1)
            }
            deviceLocale.language.equals("fr", ignoreCase = true) -> {
                // Pour la France, on utilise Locale.FRANCE et on envoie une valeur spécifique (ici 2)
                CommandManager.getInstance().putCallChangeLanguageTask(2)
            }
            else -> {
                CommandManager.getInstance().putCallChangeLanguageTask(0)
            }
        }
        tts.language = if (deviceLocale.language.equals("fr", ignoreCase = true)) {
            java.util.Locale.FRANCE
        } else {
            deviceLocale
        }
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
        // Suppression de l'appel au suivi analytique externe
    }
}
