package com.origamilabs.orii.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.db.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel partagé entre les Fragments et l'Activity.
 * Gère la connexion Orii et l'autoScan persisté via DataStore.
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionManager: ConnectionManager
) : ViewModel() {

    companion object {
        private const val TAG = "SharedViewModel"
    }

    private val _connectionState = MutableLiveData<String>()
    val connectionState: LiveData<String> get() = _connectionState

    private var autoScanEnabled: Boolean = false

    init {
        // État de connexion initial
        _connectionState.value = if (connectionManager.isOriiConnected()) "connected" else "disconnected"

        // On lit la valeur autoScanEnabled dans DataStore
        viewModelScope.launch {
            SettingsDataStore.getAutoScanEnabled(context).collect { enabled ->
                autoScanEnabled = enabled
                Timber.d("autoScanEnabled lu depuis DataStore = $enabled")

                // Si la lecture = true et Orii pas connectée, on tente la connexion
                if (enabled && !connectionManager.isOriiConnected()) {
                    retryConnectOrii()
                }
            }
        }
    }

    /**
     * Lance un scan + tentative de connexion à Orii.
     */
    fun retryConnectOrii() {
        _connectionState.value = "connecting"
        viewModelScope.launch {
            val success = connectionManager.scanAndConnectOriiDevice()
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.d("Orii device found and connected")
                    _connectionState.value = "connected"
                } else {
                    Timber.d("Orii device not found")
                    _connectionState.value = "disconnected"
                }
            }
        }
    }

    /**
     * Arrête le scan de périphériques Orii.
     */
    fun stopSearchingOrii() {
        connectionManager.stopScan()
        _connectionState.value = if (connectionManager.isOriiConnected()) "connected" else "disconnected"
        Timber.d("Stopped scanning for Orii")
    }

    /**
     * Active/désactive l'auto-scan. Cette valeur est persistée via DataStore.
     */
    fun setAutoScan(enable: Boolean) {
        viewModelScope.launch {
            // On enregistre la valeur dans DataStore
            SettingsDataStore.setAutoScanEnabled(context, enable)
        }
        // Mise à jour de l'état local
        autoScanEnabled = enable
        Timber.d("Auto-scan set to $enable")

        // Logique existante : si on active et qu'on n'est pas connecté, on lance un scan
        if (enable && !connectionManager.isOriiConnected()) {
            retryConnectOrii()
        } else if (!enable && !connectionManager.isOriiConnected()) {
            connectionManager.stopScan()
        }
    }
}
