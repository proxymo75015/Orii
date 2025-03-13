package com.origamilabs.orii.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.db.SettingsDataStore
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appService: AppService,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    // LiveData de l'icône de batterie (déjà existant)
    private val _batteryLevel = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = _batteryLevel

    // Lecture en continu du micMode via DataStore
    private val _micMode = MutableLiveData<Int>()
    val micMode: LiveData<Int> = _micMode

    val welcomeMessage: String = resourceProvider.homeWelcome
    val soundTestPlayText: String = resourceProvider.homeSoundTestPlay
    val soundTestStopText: String = resourceProvider.homeSoundTestStop

    init {
        // Existent déjà
        if (connectionManager.isOriiConnected()) {
            appService.addListener(appServiceListener)
        }
        if (AppManager.getBatteryLevel() != -1) {
            _batteryLevel.postValue(AppManager.getBatteryLevel())
        }

        // NOUVEAU : on récupère le micMode en DataStore
        viewModelScope.launch {
            SettingsDataStore.getMicMode(context).collect { mode ->
                _micMode.postValue(mode)
            }
        }
    }

    fun getFirmwareText(): String {
        val firmwareVersionText = if (AppManager.getFirmwareVersion() == -1)
            "N/A"
        else
            AppManager.getFirmwareVersion().toString()
        return resourceProvider.helpFirmware(firmwareVersionText)
    }

    // Méthode pour mettre à jour le micMode
    fun setMicMode(mode: Int) {
        viewModelScope.launch {
            SettingsDataStore.setMicMode(context, mode)
        }
    }

    // Exemple: listener pour la batterie
    private val appServiceListener = object : AppService.AppServiceListener {
        override fun onDataReceived(intent: Intent) {
            if (intent.action == CommandManager.ACTION_BATTERY_LEVEL) {
                val level = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                _batteryLevel.postValue(level)
                Timber.d("Battery level received: $level%")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("HomeViewModel onCleared: removing AppService listener")
        appService.removeListener(appServiceListener)
    }
}
