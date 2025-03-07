package com.origamilabs.orii.core.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber

abstract class ConnectionHandler(
    @ApplicationContext protected val mContext: Context,
    private val threadName: String,
    private var mCallback: Callback?
) {

    companion object {
        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
        private const val STATE_DISCONNECTING = 3
        private const val TRIGGER_INTERVAL: Long = 5000

        fun getConnectionStateString(state: Int): String = when (state) {
            STATE_DISCONNECTED -> "ÉTAT DÉCONNECTÉ"
            STATE_CONNECTING -> "ÉTAT EN CONNEXION"
            STATE_CONNECTED -> "ÉTAT CONNECTÉ"
            STATE_DISCONNECTING -> "ÉTAT DE DÉCONNEXION"
            else -> "ÉTAT INCONNU"
        }
    }

    protected var mDevice: BluetoothDevice? = null
    protected var mCurrentState: Int = STATE_DISCONNECTED

    interface Callback {
        fun onStateChanged(oldState: Int, newState: Int)
    }

    abstract fun close()
    abstract fun connect(device: BluetoothDevice)
    abstract fun disconnect()
    protected abstract fun getConnectionState(): Int

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun isConnecting(): Boolean = (mCurrentState == STATE_CONNECTING)
    fun isConnected(): Boolean = (mCurrentState == STATE_CONNECTED)

    fun start() {
        scope.launch {
            while (isActive) {
                val state = getConnectionState()
                withContext(Dispatchers.Main) {
                    tryUpdateState(state)
                }
                delay(TRIGGER_INTERVAL)
            }
        }
    }

    fun stop() {
        mCurrentState = STATE_DISCONNECTED
        scope.cancel()
    }

    fun setDevice(device: BluetoothDevice) {
        mDevice = device
    }

    private fun tryUpdateState(state: Int) {
        Timber.d(getConnectionStateString(state))
        updateState(mCurrentState, state)
        mCurrentState = state
    }

    private fun updateState(oldState: Int, newState: Int) {
        mCallback?.onStateChanged(oldState, newState)
    }

    fun getCurrentState(): Int = mCurrentState
}
