package com.origamilabs.orii.ui

import timber.log.Timber
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.manager.AppManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel partagé entre les fragments et l'activité.
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val connectionManager: ConnectionManager
) : ViewModel() {

    companion object {
        private const val TAG = "SharedViewModel"
    }

    private val _connectionState = MutableLiveData<String>()
    val connectionState: LiveData<String> get() = _connectionState

    private var autoScanEnabled: Boolean = false

    init {
        _connectionState.value = if (connectionManager.isOriiConnected()) "connected" else "disconnected"
    }

    fun retryConnectOrii() {
        _connectionState.value = "connecting"
        viewModelScope.launch {
            val success = connectionManager.scanAndConnectOriiDevice()
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.d("Orii device found and connected")
                    _connectionState.value = "connected"
                    AppManager.setFirmwareVersionChecked(false)
                } else {
                    Timber.d("Orii device not found")
                    _connectionState.value = "disconnected"
                }
            }
        }
    }

    fun stopSearchingOrii() {
        // Appel à la méthode stopScan() ajoutée dans ConnectionManager.
        connectionManager.stopScan()
        _connectionState.value = if (connectionManager.isOriiConnected()) "connected" else "disconnected"
        Timber.d("Stopped scanning for Orii")
    }

    fun setAutoScan(enable: Boolean) {
        autoScanEnabled = enable
        if (enable && !connectionManager.isOriiConnected()) {
            retryConnectOrii()
        } else if (!enable && !connectionManager.isOriiConnected()) {
            connectionManager.stopScan()
        }
        Timber.d("Auto-scan set to $enable")
    }
}
