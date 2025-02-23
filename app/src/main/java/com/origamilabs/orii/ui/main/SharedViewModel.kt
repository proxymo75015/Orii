package com.origamilabs.orii.ui.main

import android.bluetooth.BluetoothDevice
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.core.bluetooth.manager.ScanManager
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager

/**
 * ViewModel partagé gérant la communication Bluetooth avec Orii.
 *
 * Ce ViewModel implémente [ScanManager.OnStateChangedListener] pour réagir aux événements
 * de scan, gère un timer de connexion et met à jour les états d'affichage de l'interface utilisateur.
 */
class SharedViewModel : ViewModel(), ScanManager.OnStateChangedListener {

    companion object {
        private const val TAG = "SharedViewModel"
        private const val CONNECTING_TIMEOUT_MS = 45_000L
        private const val COUNTDOWN_INTERVAL_MS = 1_000L
        private const val INITIAL_COUNTDOWN = 45
    }

    var autoScan: Boolean = true

    // État actuel de la connexion (LiveData pour DataBinding)
    private val _currentState = MutableLiveData(State.SCANNING)
    val currentState: LiveData<State> get() = _currentState

    // LiveData pour afficher un compteur dans l’UI
    private val _countDownNumber = MutableLiveData(INITIAL_COUNTDOWN)
    val countDownNumber: LiveData<Int> get() = _countDownNumber

    // Callbacks pour les événements de scan Bluetooth.
    // Initialisés par défaut avec une lambda vide pour éviter d'éventuelles erreurs.
    var oriiFoundCallback: () -> Unit = {}
    var scanTimeoutCallback: () -> Unit = {}

    private var timerRunning: Boolean = false
    private var connectingTimer: CountDownTimer? = null

    // Callback pour la gestion de l'état de connexion.
    private val connectionCallback = object : ConnectionManager.Callback {
        override fun onOriiStateChange(prevState: Int, newState: Int) {
            if (newState != prevState) {
                when (newState) {
                    0 -> {
                        AnalyticsManager.logBleStatus(AnalyticsManager.BleState.DISCONNECTED)
                    }
                    1 -> {
                        AnalyticsManager.logBleStatus(AnalyticsManager.BleState.CONNECTING)
                        _currentState.postValue(State.CONNECTING)
                        startConnectingTimer()
                    }
                    2 -> {
                        AnalyticsManager.logBleStatus(AnalyticsManager.BleState.CONNECTED)
                        _currentState.postValue(State.CONNECTED)
                        cancelConnectingTimer()
                        initOriiSettings()
                    }
                }
            }
        }

        override fun onOriiRemoveBond() {
            if (autoScan) {
                ScanManager.getInstance().start()
            }
        }

        override fun onA2dpStateChange(prevState: Int, newState: Int) {}
        override fun onGattStateChange(prevState: Int, newState: Int) {}
        override fun onHeadsetStateChange(p0: Int, p1: Int) {}
    }

    /**
     * Enumération des états possibles de la connexion Bluetooth.
     */
    enum class State {
        CONNECTED,
        CONNECTING,
        SCANNING,
        TIMEOUT
    }

    init {
        addConnectionCallback()
        ScanManager.getInstance().setOnStateChangedListener(this)
        refresh()
    }

    /**
     * Ajoute le callback de connexion à [ConnectionManager].
     */
    fun addConnectionCallback() {
        ConnectionManager.getInstance().addCallback(connectionCallback)
    }

    /**
     * Retire le callback de connexion de [ConnectionManager].
     */
    fun removeConnectionCallback() {
        ConnectionManager.getInstance().removeCallback(connectionCallback)
    }

    /**
     * Initialise les paramètres d’Orii après une connexion réussie.
     */
    fun initOriiSettings() {
        val connectionManager = ConnectionManager.getInstance()
        if (connectionManager.isOriiConnected()) {
            val sharedPrefs = AppManager.sharedPreferences
            CommandManager.getInstance().apply {
                putCallSwitchMicModeTask(sharedPrefs.getMicMode())
                putCallChangeGestureModeTask(sharedPrefs.getGestureMode())
                putCallChangeSensitivityOfGestureTask(sharedPrefs.getSensitivityOfGesture())
            }
        }
    }

    /**
     * Rafraîchit l'état actuel de la connexion en fonction de [ConnectionManager].
     */
    private fun refresh() {
        val connectionManager = ConnectionManager.getInstance()
        when (connectionManager.getConnectionState()) {
            1 -> _currentState.postValue(State.CONNECTING)
            2 -> _currentState.postValue(State.CONNECTED)
        }
    }

    /**
     * Lance une nouvelle recherche de l’appareil Orii.
     *
     * @param oriiFoundCallback Callback appelée lorsque l’appareil est trouvé.
     * @param scanTimeoutCallback Callback appelée en cas de dépassement de temps.
     */
    fun scan(oriiFoundCallback: () -> Unit, scanTimeoutCallback: () -> Unit) {
        this.oriiFoundCallback = oriiFoundCallback
        this.scanTimeoutCallback = scanTimeoutCallback
        _currentState.postValue(State.SCANNING)
        ConnectionManager.getInstance().close()
        ScanManager.getInstance().start()
        startConnectingTimer()
    }

    /**
     * Arrête la recherche de l’appareil Orii et met à jour l’état.
     */
    fun stopSearchingOrii() {
        ConnectionManager.getInstance().stopSearch()
        cancelConnectingTimer()
        _currentState.postValue(State.TIMEOUT)
    }

    /**
     * Callback invoqué lors du dépassement du temps de scan.
     */
    override fun onScanTimeout() {
        scanTimeoutCallback.invoke()
        _currentState.postValue(State.TIMEOUT)
        cancelConnectingTimer()
    }

    /**
     * Callback invoqué lorsqu'un appareil Orii est trouvé.
     *
     * @param orii L’appareil Bluetooth trouvé.
     */
    override fun onOriiFound(orii: BluetoothDevice) {
        ConnectionManager.getInstance().start(orii, connectionCallback)
    }

    /**
     * Démarre le timer de connexion si ce n’est pas déjà le cas.
     * Pour chaque démarrage, une nouvelle instance de CountDownTimer est créée.
     */
    fun startConnectingTimer() {
        if (timerRunning) return

        _currentState.postValue(State.CONNECTING)
        _countDownNumber.postValue(INITIAL_COUNTDOWN)

        connectingTimer = object : CountDownTimer(CONNECTING_TIMEOUT_MS, COUNTDOWN_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                _countDownNumber.postValue((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                _currentState.postValue(State.TIMEOUT)
                timerRunning = false
                connectingTimer = null
            }
        }.also {
            it.start()
            timerRunning = true
        }
    }

    /**
     * Annule le timer de connexion.
     */
    fun cancelConnectingTimer() {
        connectingTimer?.cancel()
        timerRunning = false
        connectingTimer = null
    }

    override fun onCleared() {
        super.onCleared()
        removeConnectionCallback()
    }
}
