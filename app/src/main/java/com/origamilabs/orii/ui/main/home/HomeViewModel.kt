package com.origamilabs.orii.ui.main.home

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
 * ViewModel pour le Home (Accueil).
 *
 * Il gère la réception des données du service d'application (niveau de batterie)
 * et vérifie la version de l'application en lisant un fichier JSON dans le dossier "update".
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val MIN_VERSION_CODE = 120
    }

    // LiveData pour le niveau de batterie.
    private val batteryLevel = MutableLiveData<Int>()

    // LiveData pour l'information de version de l'application.
    private val appVersionInfo = MutableLiveData<AppVersionInfo>()

    // Listener pour recevoir les données du service d'application.
    private val appServiceListener = object : AppService.AppServiceListener {
        override fun onDataReceived(intent: Intent) {
            val action = intent.action
            if (action != null && action.hashCode() == -726745125 && action == CommandManager.ACTION_BATTERY_LEVEL) {
                batteryLevel.postValue(intent.getIntExtra(CommandManager.EXTRA_DATA, -1))
            }
        }
    }

    init {
        // Si un périphérique orii est connecté, ajoute le listener au service.
        val connectionManager = ConnectionManager.getInstance()
        if (connectionManager.isOriiConnected()) {
            val appService = (getApplication() as MainApplication).appService
            appService?.addListener(appServiceListener)
        }
        // Met à jour le niveau de batterie s'il est disponible.
        if (AppManager.batteryLevel != -1) {
            batteryLevel.postValue(AppManager.batteryLevel)
        }
    }

    fun getBatteryLevel(): MutableLiveData<Int> = batteryLevel
    fun getAppVersionInfo(): MutableLiveData<AppVersionInfo> = appVersionInfo

    /**
     * Vérifie la version de l'application en lisant le fichier "ORII.json" dans le dossier "update".
     * Si la version de l'application est supérieure au seuil défini, met à jour le LiveData.
     */
    fun checkAppVersion() {
        // Récupère le chemin du dossier "update" dans le répertoire des fichiers externes de l'application.
        val externalFilesDir = getApplication<MainApplication>().applicationContext.getExternalFilesDir("update")
        val filePath = externalFilesDir?.absolutePath
        val file = File(filePath, "ORII.json")
        
        // Lit le contenu du fichier.
        val jsonString = FileUtils.getStringFromFile(file)
        val gson = Gson()
        val versionInfo = gson.fromJson(jsonString, AppVersionInfo::class.java)

        // Détermine le contenu commun en fonction de la langue par défaut.
        val currentLanguage = Locale.getDefault().language ?: "en"
        val commonContent = when (currentLanguage) {
            "zh" -> versionInfo.language.zh
            "ja" -> versionInfo.language.ja
            "en" -> versionInfo.language.en
            "fr" -> versionInfo.language.fr  // Ajout du français
            else -> versionInfo.language.en
        }
        versionInfo.language.common = commonContent

        // Si la version lue est supérieure au seuil MIN_VERSION_CODE, on met à jour le LiveData.
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
