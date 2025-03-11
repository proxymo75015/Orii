package com.origamilabs.orii.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.data.DeviceUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// Définition d'un état de connexion
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val deviceUserRepository: DeviceUserRepository
) : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    init {
        _userName.value = deviceUserRepository.getLocalUserName()
        _connectionState.value = ConnectionState.Disconnected
    }

    fun connectToOrii(address: String) {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Connecting
            delay(2000) // Simule un délai de connexion
            _connectionState.value = ConnectionState.Connected
        }
    }

    fun disconnectOrii() {
        _connectionState.value = ConnectionState.Disconnected
    }
}
