package com.origamilabs.orii.services

import android.app.*
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
import androidx.core.app.NotificationCompat
import com.origamilabs.orii.Constants
import com.origamilabs.orii.controller.DeviceController
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.db.CallLog
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.VoiceAssistantCounter
import com.origamilabs.orii.models.enum.CustomCommandAction
import com.origamilabs.orii.notification.OriiNotificationListenerService
import com.origamilabs.orii.notification.listeners.OnIncomingCallReceivedListener
import com.origamilabs.orii.notification.listeners.OnNotificationReceivedListener
import com.origamilabs.orii.notification.listeners.OnSmsReceivedListener
import com.origamilabs.orii.notification.receivers.IncomingCallReceiver
import com.origamilabs.orii.notification.receivers.NotificationReceiver
import com.origamilabs.orii.notification.receivers.PhoneCallReceiver
import com.origamilabs.orii.notification.receivers.SmsReceiver
import com.origamilabs.orii.utils.CalendarContentResolver
import com.origamilabs.orii.utils.ResourceProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AppService : Service(),
    OnSmsReceivedListener,
    OnNotificationReceivedListener,
    OnIncomingCallReceivedListener {

    companion object {
        private const val TAG = "AppService"
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }

    @javax.inject.Inject
    lateinit var resourceProvider: ResourceProvider

    @javax.inject.Inject
    lateinit var deviceController: DeviceController

    private var smsReceiver: SmsReceiver? = null
    private var notificationReceiver: NotificationReceiver? = null
    private var incomingCallReceiver: IncomingCallReceiver? = null
    private var phoneCallReceiver: PhoneCallReceiver? = null

    private var tts: TextToSpeech? = null
    private var isLowBatteryNotified = false

    private val listeners = mutableSetOf<AppServiceListener>()
    private val binder = LocalBinder()

    // Portée des coroutines (pour tâches en arrière-plan dans le service)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Interface de rappel pour informer d’autres composants
     * lorsque des données (events Bluetooth/commandes) sont reçues.
     */
    interface AppServiceListener {
        fun onDataReceived(intent: Intent)
    }

    fun addListener(listener: AppServiceListener) = listeners.add(listener)
    fun removeListener(listener: AppServiceListener) = listeners.remove(listener)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannelIfNeeded()
        ensureNotificationListenerServiceIsRunning()
        registerAllReceivers()
        setupTTS()

        // Ajout des callbacks
        ConnectionManager.addCallback(connectionCallback)
        CommandManager.addCallback(commandCallback)
    }

    /**
     * Callback Bluetooth (ConnectionManager)
     */
    private val connectionCallback = object : ConnectionManager.Callback {
        override fun onA2dpStateChange(oldState: Int, newState: Int) = Unit
        override fun onGattStateChange(oldState: Int, newState: Int) = Unit
        override fun onHeadsetStateChange(oldState: Int, newState: Int) = Unit
        override fun onOriiRemoveBond() = Unit

        override fun onOriiStateChange(prevState: Int, newState: Int) {
            // Ex.: si l’état passe à 0 (non connecté), on retire la notif “Batterie faible”
            if (newState == 0) removeLowBatteryNotification(this@AppService)
        }
    }

    /**
     * Callback pour les commandes reçues (CommandManager)
     */
    private val commandCallback = object : CommandManager.Callback {
        override fun onDataReceived(intent: Intent) {
            when (intent.action) {
                CommandManager.ACTION_CHECK_MIC_MODE -> {
                    val micMode = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                    AppManager.sharedPreferences.setMicMode(micMode)
                }

                CommandManager.ACTION_CHECK_LANGUAGE -> {
                    Timber.d("Langue ORII: ${intent.getIntExtra(CommandManager.EXTRA_DATA, -1)}")
                    tts?.let { setTTSLanguage(it) }
                }

                CommandManager.ACTION_VOICE_ASSISTANT_COUNTER -> {
                    val count = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                    insertVoiceAssistantTriggerCount(count)
                }

                CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP -> {
                    triggerCustomCommandAction(CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP)
                }

                CommandManager.ACTION_CHECK_SENSITIVITY_OF_GESTURE -> {
                    val sensitivity = intent.getIntExtra(CommandManager.EXTRA_DATA, 0)
                    AppManager.sharedPreferences.setSensitivityOfGesture(sensitivity)
                }

                CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP -> {
                    triggerCustomCommandAction(CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP)
                }

                CommandManager.ACTION_SINGLE_BUTTON_DOUBLE_PRESSED -> {
                    readOutMessages()
                }

                CommandManager.ACTION_BATTERY_LEVEL -> {
                    val level = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                    AppManager.setBatteryLevel(level)
                    updateBatteryLevel(level)
                }

                CommandManager.ACTION_GESTURE_SIDE_DOUBLE_TAP -> {
                    deviceController.callMediaPlayOrPause()
                }

                CommandManager.ACTION_VOICE_ASSISTANT_STATE_CHANGED -> {
                    // 0 = stoppé, 1 = en cours
                    val isStopped = (intent.getIntExtra(CommandManager.EXTRA_DATA, -1) == 0)
                    insertVoiceAssistantTriggerCount(if (isStopped) 1 else 0)
                }

                CommandManager.ACTION_CHECK_GESTURE_MODE -> {
                    val gestureMode = intent.getIntExtra(CommandManager.EXTRA_DATA, 0)
                    AppManager.sharedPreferences.setGestureMode(gestureMode)
                }

                CommandManager.ACTION_MIC_MODE_CHANGED -> {
                    val newMicMode = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                    AppManager.sharedPreferences.setMicMode(newMicMode)
                }
            }
            // Notifier nos listeners enregistrés
            listeners.forEach { it.onDataReceived(intent) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = resourceProvider.notificationConnectionChannelId
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(resourceProvider.icStatusbar)
            .setContentTitle(resourceProvider.appName)
            .setContentText("Service Bluetooth actif")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Le param FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE est dispo à partir d’Android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): AppService = this@AppService
    }

    // ---------------------------------------------------------------------------------------
    //                             Gestion de la batterie
    // ---------------------------------------------------------------------------------------
    fun updateBatteryLevel(batteryLevel: Int) {
        // Par exemple: si <= 1%, on affiche la notif “Low Battery”
        if (batteryLevel <= 1) {
            showLowBatteryNotification(this)
        } else {
            removeLowBatteryNotification(this)
        }
    }

    private fun showLowBatteryNotification(context: Context) {
        if (isLowBatteryNotified) return

        val notifManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = resourceProvider.notificationBatteryChannelId

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(resourceProvider.icStatusbar)
            .setContentTitle(resourceProvider.notificationTitleBatteryLow)
            .setContentText(resourceProvider.notificationTextBatteryLow)
            .setAutoCancel(true)
            .setOngoing(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                resourceProvider.notificationConnectionChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notifManager.createNotificationChannel(channel)
        }

        notifManager.notify(Constants.ORII_NOTIFICATION_LOW_BATTERY, builder.build())
        isLowBatteryNotified = true
    }

    fun removeLowBatteryNotification(context: Context) {
        val notifManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.cancel(Constants.ORII_NOTIFICATION_LOW_BATTERY)
        isLowBatteryNotified = false
    }

    // ---------------------------------------------------------------------------------------
    //                      Actions personnalisées (gestes, etc.)
    // ---------------------------------------------------------------------------------------
    fun triggerCustomCommandAction(triggerGesture: String) {
        val actionType = when (triggerGesture) {
            CommandManager.ACTION_GESTURE_FLAT_TRIPLE_TAP ->
                AppManager.sharedPreferences.flatTripleTapAction

            CommandManager.ACTION_GESTURE_REVERSE_DOUBLE_TAP ->
                AppManager.sharedPreferences.reverseDoubleTapAction

            else -> CustomCommandAction.WEB_HOOK
        }

        when (actionType) {
            CustomCommandAction.WEB_HOOK -> {
                // Déclenchement d’une action custom (ex. requête Web)
                Timber.d("Action WEB_HOOK déclenchée")
            }
            CustomCommandAction.DO_NOT_DISTURB_MODE -> deviceController.switchDisturbMode()
            CustomCommandAction.FLASHLIGHT_SWITCH -> deviceController.switchFlashlight()
            CustomCommandAction.SCREEN_ON_OFF -> deviceController.switchScreenLock()
            CustomCommandAction.TIME_READOUT -> speakCurrentTime(false)
            CustomCommandAction.CALENDAR_READOUT -> speakCalendarEvent()
            else -> Timber.d("Action non gérée: $actionType")
        }
    }

    private fun speakCurrentTime(isShort: Boolean) {
        val locale = Locale.FRANCE
        val pattern = if (isShort) "HH:mm" else "EEEE, dd MMMM, HH:mm"
        val sdf = SimpleDateFormat(pattern, locale)
        val currentTime = sdf.format(Date())
        readOutMessages(currentTime)
    }

    private fun speakCalendarEvent() {
        val events = CalendarContentResolver.getTodayEvent(this)
        val sb = StringBuilder()
        for (event in events) {
            if (event.allDay) {
                sb.append(getStringByName("tts_message_all_day"))
            } else {
                sb.append(event.beginTime)
            }
            sb.append(" ").append(event.event).append("\n")
        }
        readOutMessages(sb.toString())
    }

    // ---------------------------------------------------------------------------------------
    //                           Gestion de la lecture TTS
    // ---------------------------------------------------------------------------------------
    fun readOutMessages() {
        tts?.let { engine ->
            if (engine.isSpeaking) {
                engine.stop()
                readOutMessages(getStringByName("tts_message_cleared"))
                return
            }
        }

        val messages = MessageHandler().getSpeechQueue(this)
        if (messages.isNotEmpty()) {
            speakCurrentTime(isShort = true)
            messages.forEach { msg -> readOutMessages(msg) }
        } else {
            readOutMessages(getStringByName("tts_no_new_messages"))
        }
    }

    private fun readOutMessages(speech: String) {
        tts?.let { engine ->
            setTTSLanguage(engine)
            Timber.d("TTS read out: $speech")
            engine.setSpeechRate(AppManager.sharedPreferences.readoutSpeed)
            engine.speak(speech, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    private fun setTTSLanguage(tts: TextToSpeech) {
        tts.language = Locale.FRANCE
        // On peut aussi envoyer une commande BT pour forcer la langue côté ORII
        CommandManager.putCallChangeLanguageTask(0)
    }

    private fun getStringByName(resourceName: String): String {
        val resourceId = resources.getIdentifier(resourceName, "string", packageName)
        require(resourceId != 0) { "La ressource '$resourceName' n'a pas été trouvée." }
        return resourceProvider.getString(resourceId)
    }

    // ---------------------------------------------------------------------------------------
    //                        Insertion en BD (ex. compteur assistant vocal)
    // ---------------------------------------------------------------------------------------
    fun insertVoiceAssistantTriggerCount(times: Int) {
        if (times == 0) return
        AppManager.runQueryOnBackground {
            AppManager.database.vaCounterDao().insert(
                VoiceAssistantCounter(
                    times,
                    (System.currentTimeMillis() / 1000).toInt()
                )
            )
        }
    }

    // ---------------------------------------------------------------------------------------
    //                    Gestion des appels entrants & notifications
    // ---------------------------------------------------------------------------------------
    override fun onIncomingCallReceived(packageName: String) {
        Timber.d("onIncomingCallReceived: $packageName")
        CommandManager.putCallAllowLinePhonecallPickUpTask()

        // Insérer un log d’appel en base
        serviceScope.launch {
            try {
                val callLog = CallLog(
                    packageName = packageName,
                    timestamp = System.currentTimeMillis()
                )
                AppManager.database.callLogDao().insertCallLog(callLog)
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors de l'insertion du log d'appel")
            }
        }
    }

    override fun onSmsReceived(sender: String, message: String) {
        Timber.d("onSmsReceived: $sender -> $message")
        MessageHandler().addMessage("sms", sender, message)

        AppManager.runQueryOnBackground {
            val appConf = AppManager.database.applicationDao().findByPackageName("sms")
            val contactConf = AppManager.database.personDao().findByPersonName(sender)
                ?: com.origamilabs.orii.models.Application(-1, 0, 0, "")
            if (appConf != null) {
                CommandManager.putCallMessageReceivedTask(
                    appConf.ledColor,
                    appConf.vibration,
                    contactConf.ledColor,
                    contactConf.vibration,
                    true
                )
            }
        }
    }

    override fun onNotificationReceived(packageName: String, sender: String, message: String) {
        Timber.d("onNotificationReceived: $packageName => $sender : $message")
        MessageHandler().addMessage(packageName, sender, message)

        AppManager.runQueryOnBackground {
            val appConf = AppManager.database.applicationDao().findByPackageName(packageName)
            val contactConf = AppManager.database.personDao().findByPersonName(sender)
                ?: com.origamilabs.orii.models.Application(-1, 0, 0, "")
            if (appConf != null) {
                CommandManager.putCallMessageReceivedTask(
                    appConf.ledColor,
                    appConf.vibration,
                    contactConf.ledColor,
                    contactConf.vibration,
                    true
                )
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    //               Création du canal de notif (Android O+) & gestion du service
    // ---------------------------------------------------------------------------------------
    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                resourceProvider.notificationConnectionChannelId,
                "Service ORII",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun ensureNotificationListenerServiceIsRunning() {
        val component = ComponentName(this, OriiNotificationListenerService::class.java)
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // getRunningServices est obsolète sur les versions récentes,
        // idéalement on passerait par d’autres API. Exemple simplifié ici.
        val isRunning = activityManager.getRunningServices(Int.MAX_VALUE)
            .any { it.service == component && it.pid == Process.myPid() }

        if (!isRunning) toggleNotificationListenerService()
    }

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        val comp = ComponentName(this, OriiNotificationListenerService::class.java)
        // Désactive puis réactive le service pour forcer sa relance
        pm.setComponentEnabledSetting(
            comp,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            comp,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun registerAllReceivers() {
        // SMS
        smsReceiver = SmsReceiver().also {
            registerReceiver(it, IntentFilter(Constants.ACTION_SMS_RECEIVED))
            it.setOnSmsReceivedListener(this)
        }
        // Notifications
        notificationReceiver = NotificationReceiver().also {
            registerReceiver(it, IntentFilter(Constants.ACTION_NOTIFICATION_RECEIVED))
            it.setOnNotificationReceivedListener(this)
        }
        // Appels entrants
        incomingCallReceiver = IncomingCallReceiver().also {
            registerReceiver(it, IntentFilter(Constants.ACTION_INCOMING_CALL_RECEIVED))
            it.setOnIncomingCallReceivedListener(this)
        }
        // PhoneCallReceiver (intent “android.intent.action.PHONE_STATE”)
        phoneCallReceiver = PhoneCallReceiver().also {
            registerReceiver(it, IntentFilter("android.intent.action.PHONE_STATE"))
        }
    }

    // ---------------------------------------------------------------------------------------
    //                           Gestion du TextToSpeech
    // ---------------------------------------------------------------------------------------
    private fun setupTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { setTTSLanguage(it) }
            } else {
                Timber.e("Erreur lors de l'initialisation du TTS")
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    //                              Nettoyage (onDestroy)
    // ---------------------------------------------------------------------------------------
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        runCatching { smsReceiver?.let { unregisterReceiver(it) } }
            .onFailure { Timber.e(it, "Erreur lors du désenregistrement de smsReceiver") }
        runCatching { notificationReceiver?.let { unregisterReceiver(it) } }
            .onFailure { Timber.e(it, "Erreur lors du désenregistrement de notificationReceiver") }
        runCatching { incomingCallReceiver?.let { unregisterReceiver(it) } }
            .onFailure { Timber.e(it, "Erreur lors du désenregistrement de incomingCallReceiver") }
        runCatching { phoneCallReceiver?.let { unregisterReceiver(it) } }
            .onFailure { Timber.e(it, "Erreur lors du désenregistrement de phoneCallReceiver") }

        tts?.shutdown()
        tts = null
        listeners.clear()
    }
}
