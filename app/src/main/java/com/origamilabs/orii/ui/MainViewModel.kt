package com.origamilabs.orii.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.bluetooth.OriiBluetoothManager
import com.origamilabs.orii.data.DeviceUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** ViewModel principal gérant l’état de l’application (ex: connexion Bluetooth, profil utilisateur). */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothManager: OriiBluetoothManager,
    private val userRepository: DeviceUserRepository
) : ViewModel() {

    // État de la connexion à la bague ORII
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: LiveData<ConnectionState> get() = _connectionState

    // Nom d’utilisateur local (profil Android)
    private val _userName = MutableLiveData<String>("")
    val userName: LiveData<String> get() = _userName

    init {
        // Charger le nom du profil utilisateur local (opération rapide, peut être faite sur IO par sécurité)
        viewModelScope.launch {
            _userName.postValue(userRepository.getLocalUserName())
        }
    }

    /** Demande de connexion à la bague ORII (adresse MAC connue ou sélectionnée). */
    fun connectToOrii(deviceAddress: String) {
        _connectionState.value = ConnectionState.Connecting
        viewModelScope.launch {
            try {
                bluetoothManager.connect(deviceAddress)
                _connectionState.postValue(ConnectionState.Connected)
            } catch (e: Exception) {
                _connectionState.postValue(ConnectionState.Error("Échec de connexion : ${e.message}"))
            }
        }
    }

    /** Déconnexion manuelle de la bague. */
    fun disconnectOrii() {
        bluetoothManager.disconnect()
        _connectionState.value = ConnectionState.Disconnected
    }
}

/** États possibles de la connexion Bluetooth à la bague (pour l’UI). */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
