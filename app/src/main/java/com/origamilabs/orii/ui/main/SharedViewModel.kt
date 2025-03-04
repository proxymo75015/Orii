package com.origamilabs.orii.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel partagé entre les principaux fragments (Home, Alerts, etc.) et l'activité.
 * Gère l'état global de la connexion Orii et les actions asynchrones sur la bague.
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val connectionManager: ConnectionManager
) : ViewModel() {

    companion object {
        private const val TAG = "SharedViewModel"
    }

    // État de connexion de la bague (ex: "connected", "connecting", "disconnected")
    private val _connectionState = MutableLiveData<String>()
    val connectionState: LiveData<String> get() = _connectionState

    // Indique si une mise à jour firmware est disponible (issu de AppManager ou d'un check local)
    private val _canFirmwareUpdate = MutableLiveData<Boolean>()
    val canFirmwareUpdate: LiveData<Boolean> get() = _canFirmwareUpdate

    // Indicateur de scan automatique continu
    private var autoScanEnabled: Boolean = false

    init {
        // État initial de la connexion
        _connectionState.value = if (connectionManager.isOriiConnected()) {
            "connected"
        } else {
            "disconnected"
        }
        // État initial de disponibilité de mise à jour
        _canFirmwareUpdate.value = AppManager.canFirmwareUpdate
    }

    /**
     * Lance (ou relance) le processus de scan et connexion à la bague Orii.
     * Utilise une coroutine pour ne pas bloquer le thread UI.
     */
    fun retryConnectOrii() {
        // Met à jour l'état (pour l'UI, on pourrait afficher "Connexion en cours...")
        _connectionState.value = "connecting"
        viewModelScope.launch {
            // Appelle le ConnectionManager pour scanner et se connecter (suspend function)
            val success = connectionManager.scanAndConnectOriiDevice()
            withContext(Dispatchers.Main) {
                if (success) {
                    Log.d(TAG, "Orii device found and connected")
                    _connectionState.value = "connected"
                    AppManager.firmwareVersionChecked = false  // on pourra rechecker firmware après connexion
                } else {
                    Log.d(TAG, "Orii device not found (timeout or failure)")
                    _connectionState.value = "disconnected"
                }
            }
        }
    }

    /**
     * Stoppe la recherche en cours de la bague Orii.
     */
    fun stopSearchingOrii() {
        connectionManager.stopScan()
        _connectionState.value = if (connectionManager.isOriiConnected()) "connected" else "disconnected"
        Log.d(TAG, "Stopped scanning for Orii")
    }

    /**
     * Active ou désactive le scan automatique de la bague.
     * Si activé et pas connecté, lance immédiatement une tentative de connexion.
     */
    fun setAutoScan(enable: Boolean) {
        autoScanEnabled = enable
        if (enable) {
            if (!connectionManager.isOriiConnected()) {
                retryConnectOrii()
            }
            // Sinon, si déjà connecté, on ne fait rien de plus.
        } else {
            // Désactivation : on peut éventuellement arrêter le scan en cours
            if (!connectionManager.isOriiConnected()) {
                connectionManager.stopScan()
            }
        }
        Log.d(TAG, "Auto-scan set to $enable")
    }

    /**
     * Met à jour l'indicateur de disponibilité d'une mise à jour firmware.
     * Appelé par AppService ou autre composant interne une fois qu'il détecte un firmware à jour.
     */
    fun notifyFirmwareUpdateAvailable(available: Boolean) {
        _canFirmwareUpdate.postValue(available)
        AppManager.canFirmwareUpdate = available
    }
}
