package com.origamilabs.orii.ui.main.help.ui.help

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.AppVersionInfo
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.ui.MainApplication
import com.origamilabs.orii.utils.FileUtils
import java.io.File
import java.util.Locale

/**
 * ViewModel pour le firmware test.
 *
 * Gère la mise à jour de l'information de version de l'application ainsi que le suivi du niveau de batterie.
 * Il écoute les données du service d'application et met à jour les LiveData en conséquence.
 */
class FirmwareTestViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "FirmwareTestViewModel"
        private const val MIN_VERSION_CODE = 120
    }

    // Listener pour recevoir les données du service d'application
    private val appServiceListener = object : AppService.AppServiceListener {
        override fun onDataReceived(intent: Intent) {
            val action = intent.action
            if (action != null && action.hashCode() == -726745125 && action == CommandManager.ACTION_BATTERY_LEVEL) {
                batteryLevel.postValue(intent.getIntExtra(CommandManager.EXTRA_DATA, -1))
            }
        }
    }

    private val appVersionInfo = MutableLiveData<AppVersionInfo>()
    private val batteryLevel = MutableLiveData<Int>()

    fun getBatteryLevel(): MutableLiveData<Int> = batteryLevel
    fun getAppVersionInfo(): MutableLiveData<AppVersionInfo> = appVersionInfo

    init {
        // Si un périphérique orii est connecté, ajouter le listener au service
        val connectionManager = ConnectionManager.getInstance()
        if (connectionManager.isOriiConnected()) {
            val appService = (getApplication() as MainApplication).appService
            appService?.addListener(appServiceListener)
        }
        // Mettre à jour le niveau de batterie si disponible
        if (AppManager.batteryLevel != -1) {
            batteryLevel.postValue(AppManager.batteryLevel)
        }
    }

    /**
     * Vérifie la version de l'application en lisant le fichier JSON "ORII.json" dans le dossier "update".
     *
     * Si la version de l'app est supérieure au seuil défini, met à jour le LiveData [appVersionInfo].
     */
    fun checkAppVersion() {
        val externalFilesDir = getApplication<MainApplication>().applicationContext.getExternalFilesDir("update")
        val filePath = externalFilesDir?.absolutePath
        val file = File(filePath, "ORII.json")
        val jsonString = FileUtils.getStringFromFile(file)
        val gson = Gson()
        val versionInfo = gson.fromJson(jsonString, AppVersionInfo::class.java)

        // Détermine le contenu commun en fonction de la langue par défaut
        val language = Locale.getDefault().language ?: "en"
        val commonContent = when (language) {
            "zh" -> versionInfo.language.zh
            "ja" -> versionInfo.language.ja
            "en" -> versionInfo.language.en
            else -> versionInfo.language.en
        }
        versionInfo.language.common = commonContent

        if (versionInfo.versionCode > MIN_VERSION_CODE) {
            appVersionInfo.postValue(versionInfo)
        }
    }

    /**
     * Ajoute le listener du service d'application.
     *
     * @return Le résultat de l'ajout du listener, ou null si le service n'est pas disponible.
     */
    fun addServiceListener(): Boolean? {
        val appService = (getApplication() as MainApplication).appService
        return appService?.addListener(appServiceListener)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        val appService = (getApplication() as MainApplication).appService
        appService?.removeListener(appServiceListener)
    }
}
