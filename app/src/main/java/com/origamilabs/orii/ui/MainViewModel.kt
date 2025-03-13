package com.origamilabs.orii.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origamilabs.orii.db.SettingsDataStore
import com.origamilabs.orii.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    init {
        // Lecture du User en DataStore, on récupère son nom pour l’afficher
        viewModelScope.launch {
            SettingsDataStore.getUser(context).collect { user ->
                _userName.postValue(user?.name ?: "Unknown user")
            }
        }
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

    // Exemple: méthode pour sauvegarder le User en DataStore
    fun saveUser(user: User) {
        viewModelScope.launch {
            SettingsDataStore.setUser(context, user)
        }
    }
}
