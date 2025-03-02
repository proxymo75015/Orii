package com.origamilabs.orii.ui.main.home

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.AppVersionInfo
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.ui.MainApplication
import com.origamilabs.orii.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel pour le Home (Accueil).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val connectionManager: ConnectionManager // <-- Injection Hilt
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val MIN_VERSION_CODE = 120
    }

    private val _batteryLevel = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = _batteryLevel

    private val _appVersionInfo = MutableLiveData<AppVersionInfo>()
    val appVersionInfo: LiveData<AppVersionInfo> get() = _appVersionInfo

    private val appServiceListener = object : AppService.AppServiceListener {
        override fun onDataReceived(intent: Intent) {
            val action = intent.action
            if (action != null && action.hashCode() == -726745125 && action == CommandManager.ACTION_BATTERY_LEVEL) {
                _batteryLevel.postValue(intent.getIntExtra(CommandManager.EXTRA_DATA, -1))
            }
        }
    }

    init {
        // Avant : if (ConnectionManager.isOriiConnected()) { ... }
        // Maintenant : on utilise l'instance injectée
        if (connectionManager.isOriiConnected()) {
            val mainApp = getApplication<MainApplication>()
            mainApp.appService?.addListener(appServiceListener)
        }
        // Met à jour le niveau de batterie s'il est déjà stocké
        if (AppManager.batteryLevel != -1) {
            _batteryLevel.postValue(AppManager.batteryLevel)
        }
    }

    /**
     * Vérifie la version de l'application en lisant le fichier "ORII.json".
     */
    fun checkAppVersion() {
        val externalFilesDir = getApplication<MainApplication>()
            .applicationContext
            .getExternalFilesDir("update")

        val file = File(externalFilesDir?.absolutePath, "ORII.json")
        val jsonString = FileUtils.getStringFromFile(file)
        val gson = Gson()
        val versionInfo = gson.fromJson(jsonString, AppVersionInfo::class.java)

        // Détermine le bloc "common" selon la langue courante
        val currentLanguage = Locale.getDefault().language ?: "en"
        val commonContent = when (currentLanguage) {
            "zh" -> versionInfo.language.zh
            "ja" -> versionInfo.language.ja
            "fr" -> versionInfo.language.fr
            else -> versionInfo.language.en
        }
        versionInfo.language.common = commonContent

        // Met à jour le LiveData si la version dépasse le seuil
        if (versionInfo.versionCode > MIN_VERSION_CODE) {
            _appVersionInfo.postValue(versionInfo)
        }
    }

    /**
     * Ajoute le listener du service d'application.
     */
    fun addServiceListener(): Boolean? {
        val appService = getApplication<MainApplication>().appService
        return appService?.addListener(appServiceListener)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        val appService = getApplication<MainApplication>().appService
        appService?.removeListener(appServiceListener)
    }
}
