package com.origamilabs.orii.ui.main.home

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.services.AppService
import com.origamilabs.orii.ui.MainApplication

/**
 * ViewModel pour l’écran d’accueil (Home).
 *
 * Gère la réception des données de la bague Orii via le service AppService (ex: niveau de batterie).
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    // LiveData pour le niveau de batterie de la bague Orii
    private val _batteryLevel = MutableLiveData<Int>()
    val batteryLevel: LiveData<Int> get() = _batteryLevel

    // Listener pour recevoir les Intents du service AppService
    private val appServiceListener = object : AppService.AppServiceListener {
        override fun onDataReceived(intent: Intent) {
            val action = intent.action
            if (action == CommandManager.ACTION_BATTERY_LEVEL) {
                val level = intent.getIntExtra(CommandManager.EXTRA_DATA, -1)
                _batteryLevel.postValue(level)
                Log.d(TAG, "Battery level received: $level%")
            }
            // D'autres actions peuvent être gérées ici si nécessaire
        }
    }

    init {
        // Si Orii est déjà connecté, on s’abonne aux données du service dès le début
        if (ConnectionManager.isOriiConnected()) {
            val appService = (getApplication() as MainApplication).appService
            appService?.addListener(appServiceListener)
        }
        // Initialiser le niveau de batterie si une valeur est déjà disponible dans AppManager
        if (AppManager.batteryLevel != -1) {
            _batteryLevel.postValue(AppManager.batteryLevel)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "HomeViewModel onCleared: nettoyage du listener AppService")
        // Retire le listener du service pour éviter les fuites
        val appService = (getApplication() as MainApplication).appService
        appService?.removeListener(appServiceListener)
    }
}
