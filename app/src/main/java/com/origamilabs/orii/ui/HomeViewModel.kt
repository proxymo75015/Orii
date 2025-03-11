package com.origamilabs.orii.ui

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel pour l'écran d'accueil (Home).
 * Gère la réception des données de la bague Orii via AppService.
 */
@HiltViewModel
@Suppress("StaticFieldLeak") // Suppression du warning concernant connectionManager
class HomeViewModel @Inject constructor(
    private val appService: AppService,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _batteryLevel = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = _batteryLevel

    // Obtention des chaînes via ResourceProvider (affichages pour l'utilisateur)
    val welcomeMessage: String = resourceProvider.homeWelcome
    val soundTestPlayText: String = resourceProvider.homeSoundTestPlay
    val soundTestStopText: String = resourceProvider.homeSoundTestStop

    fun getFirmwareText(): String {
        val firmwareVersionText = if (AppManager.getFirmwareVersion() == -1)
            "N/A"
        else
            AppManager.getFirmwareVersion().toString()
        return resourceProvider.helpFirmware(firmwareVersionText)
    }

    private val appServiceListener = object : AppService.AppServiceListener {
        override fun onDataReceived(intent: Intent) {
            if (intent.action == CommandManager.ACTION_BATTERY_LEVEL) {
                val level = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                _batteryLevel.postValue(level)
                Timber.d("Battery level received: $level%")
            }
        }
    }

    init {
        if (connectionManager.isOriiConnected()) {
            appService.addListener(appServiceListener)
        }
        if (AppManager.getBatteryLevel() != -1) {
            _batteryLevel.postValue(AppManager.getBatteryLevel())
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("HomeViewModel onCleared: removing AppService listener")
        appService.removeListener(appServiceListener)
    }
}
