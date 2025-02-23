package com.origamilabs.orii.manager

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.appevents.AppEventsConstants
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonArray
import com.origamilabs.orii.Constants
import com.origamilabs.orii.models.Application
import com.origamilabs.orii.models.FirmwareVersionInfo
import com.origamilabs.orii.models.User

object AnalyticsManager {

    private const val TAG = "AnalyticsManager"
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var otaUpdatingStartTime: Long = -1L
    private var otaUpdatingEndTime: Long = -1L

    /**
     * Initialise FirebaseAnalytics avec le contexte de l'application.
     */
    fun init(applicationContext: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
    }

    /**
     * Envoie un événement de test.
     */
    fun logTestEvent() {
        val bundle = Bundle().apply {
            putString("click", AppEventsConstants.EVENT_PARAM_VALUE_YES)
        }
        firebaseAnalytics.logEvent("test", bundle)
    }

    /**
     * Enregistre la connexion d’un utilisateur.
     * @param loginWay La méthode de connexion (par exemple, email, facebook, google).
     */
    fun logUserLogin(loginWay: String) {
        firebaseAnalytics.setUserProperty(PropertyKey.USER_LOGIN, loginWay)
        val currentUser: User = AppManager.currentUser
            ?: throw NullPointerException("Current user is null")
        firebaseAnalytics.setUserProperty(PropertyKey.USER_ID, currentUser.id)
        Log.d(TAG, "logUserLogin success")
    }

    /**
     * Enregistre la navigation dans le tutoriel.
     * @param activity L’activité courante.
     * @param pageNo Le numéro de page du tutoriel.
     */
    fun logTutorialFlow(activity: Activity, pageNo: Int) {
        val screenName = "${Screen.TUTORIAL_PAGE}$pageNo"
        firebaseAnalytics.setCurrentScreen(activity, screenName, screenName)
        Log.d(TAG, "logTutorialFlow success")
    }

    /**
     * Enregistre un événement de tentative de reconnexion BLE.
     */
    fun logBleRetry() {
        val bundle = Bundle()
        firebaseAnalytics.logEvent(Event.BLE_RETRY, bundle)
        Log.d(TAG, "logBleRetry success")
    }

    /**
     * Enregistre un test sonore.
     */
    fun logSoundTest() {
        val bundle = Bundle()
        firebaseAnalytics.logEvent(Event.SOUND_TEST, bundle)
        Log.d(TAG, "logSoundTest success")
    }

    /**
     * Enregistre la navigation dans la page OTA.
     * @param activity L’activité courante.
     * @param pageNo Le numéro de page (sous forme de chaîne) de la page OTA.
     */
    fun logOtaFlow(activity: Activity, pageNo: String) {
        val screenName = "${Screen.OTA_PAGE}$pageNo"
        firebaseAnalytics.setCurrentScreen(activity, screenName, screenName)
        Log.d(TAG, "logOtaFlow success")
    }

    /**
     * Enregistre le temps de mise à jour OTA.
     * Lorsqu’on passe en mode "start", on enregistre le temps de début.
     * Lorsqu’on passe en mode "end" (et si le temps de début a été défini),
     * on calcule la durée et on log l’événement OTA_UPDATE.
     * @param state Doit être ActionState.START ou ActionState.END.
     */
    fun logOtaUpdating(state: String) {
        when (state) {
            ActionState.START -> {
                otaUpdatingStartTime = System.currentTimeMillis()
            }
            ActionState.END -> {
                if (otaUpdatingStartTime != -1L) {
                    otaUpdatingEndTime = System.currentTimeMillis()
                    val duration = otaUpdatingEndTime - otaUpdatingStartTime
                    val bundle = Bundle().apply {
                        val firmwareVersionInfo: FirmwareVersionInfo = AppManager.firmwareVersionInfo
                            ?: throw NullPointerException("FirmwareVersionInfo is null")
                        putInt("version", firmwareVersionInfo.versionNumber)
                        putInt(Param.PERIOD, duration.toInt())
                    }
                    firebaseAnalytics.logEvent(Event.OTA_UPDATE, bundle)
                    otaUpdatingStartTime = -1L
                    otaUpdatingEndTime = -1L
                    Log.d(TAG, "logOtaUpdating success")
                }
            }
        }
    }

    /**
     * Enregistre la visite de la page d’alerte d’information.
     */
    fun logAlertInfoPage() {
        val bundle = Bundle()
        firebaseAnalytics.logEvent(Event.ALERT_INFO_PAGE, bundle)
        Log.d(TAG, "logAlertInfoPage success")
    }

    /**
     * Enregistre le nombre de contacts disponibles.
     */
    fun logContactCount() {
        val count = AppManager.availablePeople.size
        firebaseAnalytics.setUserProperty(PropertyKey.CONTRACT_COUNT, count.toString())
        Log.d(TAG, "logContactCount success")
    }

    /**
     * Enregistre les applications installées.
     * Construit une chaîne d’indices (séparés par ":") indiquant la position
     * de chaque application disponible dans la liste des applications supportées.
     */
    fun logInstalledApp() {
        val supportedApps = Constants.SUPPORTED_APPS.map { it.appName }
        val availableApps = AppManager.availableApps.map { it.appName }
        val indices = availableApps.mapNotNull { appName ->
            val index = supportedApps.indexOf(appName)
            if (index >= 0) index.toString() else null
        }
        val indicesStr = indices.joinToString(":")
        firebaseAnalytics.setUserProperty(PropertyKey.APP_COUNT, indicesStr)
        Log.d(TAG, "logInstalledApp success")
    }

    /**
     * Enregistre la configuration d’alerte pour un contact.
     * @param row Le numéro de ligne.
     * @param vibration Le paramètre de vibration.
     * @param ledColor La couleur de la LED.
     */
    fun logContactAlertConf(row: Int, vibration: Int, ledColor: Int) {
        val bundle = Bundle().apply {
            putInt(Param.ROW, row)
            putInt(Param.VIBRATION, vibration)
            putInt(Param.LED_COLOR, ledColor)
        }
        firebaseAnalytics.logEvent(Event.CONTACT_ALERT_CONF, bundle)
        Log.d(TAG, "logContactAlertConf success")
    }

    /**
     * Enregistre la configuration d’alerte pour une application.
     * @param appName Le nom de l’application.
     * @param vibration Le paramètre de vibration.
     * @param ledColor La couleur de la LED.
     */
    fun logAppAlertConf(appName: String, vibration: Int, ledColor: Int) {
        val bundle = Bundle().apply {
            putString("app_name", appName)
            putInt(Param.VIBRATION, vibration)
            putInt(Param.LED_COLOR, ledColor)
        }
        firebaseAnalytics.logEvent(Event.APP_ALERT_CONF, bundle)
        Log.d(TAG, "logAppAlertConf success")
    }

    /**
     * Enregistre la vitesse de lecture (readout speed) depuis les SharedPreferences.
     */
    fun logReadSpeed() {
        val readSpeed = AppManager.sharedPreferences.readoutSpeed
        firebaseAnalytics.setUserProperty(PropertyKey.READ_SPEED, readSpeed.toString())
        Log.d(TAG, "logReadSpeed success")
    }

    /**
     * Enregistre la sélection du mode micro.
     */
    fun logMicSelection() {
        val micSelection = AppManager.sharedPreferences.micMode
        firebaseAnalytics.setUserProperty(PropertyKey.MIC_SELECTION, micSelection.toString())
        Log.d(TAG, "logMicSelection success")
    }

    /**
     * Enregistre un événement d’assistance additionnelle.
     */
    fun logAdditionalSupport() {
        val bundle = Bundle()
        firebaseAnalytics.logEvent(Event.ADDITIONAL_SUPPORT, bundle)
        Log.d(TAG, "logAdditionalSupport success")
    }

    /**
     * Enregistre un retour (feedback) utilisateur.
     */
    fun logFeedback() {
        val bundle = Bundle().apply {
            putBoolean("result", true)
        }
        firebaseAnalytics.logEvent(Event.FEEDBACK, bundle)
        Log.d(TAG, "logFeedback success")
    }

    /**
     * Enregistre les déclenchements de "va" (voice assistant) en traitant un JsonArray.
     * Pour chaque objet du tableau, il extrait le nombre de déclenchements et la date.
     */
    fun logVaTriggered(jsonArray: JsonArray) {
        for (i in 0 until jsonArray.size()) {
            val jsonObject = jsonArray.get(i).asJsonObject
            val count = jsonObject.get("va_times").toString()
            val date = jsonObject.get("va_date").toString()
            val bundle = Bundle().apply {
                putString(Param.COUNT, count)
                putString(Param.DATE, date)
            }
            firebaseAnalytics.logEvent(Event.VA_TRIGGERED, bundle)
        }
        Log.d(TAG, "logVaTriggered success")
    }

    /**
     * Enregistre un appel vocal.
     * @param appPackage Le package de l’application appelante.
     */
    fun logVoiceCall(appPackage: String) {
        var source = "NULL"
        for (app in Constants.SUPPORTED_APPS) {
            if (appPackage == app.packageName) {
                source = app.appName
                break
            }
        }
        val bundle = Bundle().apply {
            putString("source", source)
        }
        firebaseAnalytics.logEvent(Event.VOICE_CALL, bundle)
        Log.d(TAG, "logVoiceCall success")
    }

    /**
     * Enregistre une lecture (readout).
     * @param appName Le nom de l’application.
     * @param length La longueur du readout.
     */
    fun logReadout(appName: String, length: Int) {
        val bundle = Bundle().apply {
            putString("source", appName)
            putInt(Param.LENGTH, length)
        }
        firebaseAnalytics.logEvent(Event.READOUT, bundle)
        Log.d(TAG, "logReadout success")
    }

    /**
     * Enregistre une erreur d'appel API.
     * Les paramètres apiName, status et description sont actuellement ignorés
     * et un message d’erreur par défaut est envoyé.
     */
    fun logAPI(apiName: String, status: String, description: String) {
        val bundle = Bundle().apply {
            putString(Param.API, "Server not response or variable is NPE")
        }
        firebaseAnalytics.logEvent(Event.API_ERROR, bundle)
        Log.d(TAG, "logAPI success")
    }

    /**
     * Enregistre l’état du Bluetooth Low Energy.
     * @param bleState L’état du BLE (par exemple, connected, connecting, disconnected).
     */
    fun logBleStatus(bleState: String) {
        firebaseAnalytics.setUserProperty(PropertyKey.BLE_STATUS, bleState)
        Log.d(TAG, "logBleStatus success")
    }

    /* Déclarations des constantes utilisées dans les logs */

    object ActionState {
        const val END = "end"
        const val START = "start"
    }

    object BleState {
        const val CONNECTED = "connected"
        const val CONNECTING = "connecting"
        const val DISCONNECTED = "disconnected"
    }

    object Event {
        const val ADDITIONAL_SUPPORT = "additional_support"
        const val ALERT_INFO_PAGE = "alert_info_page"
        const val API_ERROR = "call_api"
        const val APP_ALERT_CONF = "app_alert_conf"
        const val BLE_RETRY = "ble_retry"
        const val CONTACT_ALERT_CONF = "contact_alert_conf"
        const val FEEDBACK = "feedback"
        const val OTA_UPDATE = "ota_update"
        const val READOUT = "readout"
        const val SOUND_TEST = "sound_test"
        const val VA_TRIGGERED = "va_triggered"
        const val VOICE_CALL = "voice_call"
    }

    object LoginWay {
        const val EMAIL = "email"
        const val FACEBOOK = "facebook"
        const val GOOGLE = "google"
    }

    object Param {
        const val API = "api"
        const val APP_NAME = "app_name"
        const val COUNT = "count"
        const val DATE = "date"
        const val DESCRIPTION = "description"
        const val LED_COLOR = "color"
        const val LENGTH = "length"
        const val PERIOD = "period"
        const val ROW = "row_no"
        const val SOURCE = "source"
        const val STATUS = "status"
        const val VERSION = "version"
        const val VIBRATION = "buzz"
    }

    object PropertyKey {
        const val APP_COUNT = "app_count"
        const val BLE_STATUS = "ble_status"
        const val CONTRACT_COUNT = "contract_count"
        const val MIC_SELECTION = "mic_selection"
        const val READ_SPEED = "read_speed"
        const val USER_ID = "user_id"
        const val USER_LOGIN = "user_login"
    }

    object Screen {
        const val OTA_PAGE = "otaPage:"
        const val TUTORIAL_PAGE = "tutorialPage:"
    }
}
