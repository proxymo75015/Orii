package com.origamilabs.orii.core.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

/**
 * Classe abstraite qui gère la connexion Bluetooth.
 *
 * Elle définit des constantes d'état, démarre un thread de gestion périodique
 * pour mettre à jour l'état de la connexion, et informe un [Callback] des changements d'état.
 *
 * @property mContext Le contexte.
 * @property mCallback Le callback pour notifier les changements d'état.
 */
abstract class ConnectionHandler(
    protected val mContext: Context,
    threadName: String,
    protected var mCallback: Callback?
) {

    companion object {
        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
        const val STATE_DISCONNECTING = 3
        private const val TAG = "ConnectionHandler"
        private const val TRIGGER_INTERVAL: Long = 5000

        fun getConnectionStateString(state: Int): String = when (state) {
            STATE_DISCONNECTED -> "STATE_DISCONNECTED"
            STATE_CONNECTING -> "STATE_CONNECTING"
            STATE_CONNECTED -> "STATE_CONNECTED"
            STATE_DISCONNECTING -> "STATE_DISCONNECTING"
            else -> "UNKNOWN STATE"
        }
    }

    protected var mDevice: BluetoothDevice? = null
    protected var mHandlerThread: HandlerThread = HandlerThread(threadName).apply { start() }
    protected var mHandler: Handler = Handler(mHandlerThread.looper)
    protected var mMainHandler: Handler = Handler(mContext.mainLooper)
    private var mStopped: Boolean = false
    protected var mCurrentState: Int = STATE_DISCONNECTED

    protected val mHandlerRunnable = object : Runnable {
        override fun run() {
            tryUpdateState(getConnectionState())
            mHandler.postDelayed(this, TRIGGER_INTERVAL)
        }
    }

    interface Callback {
        fun onStateChanged(oldState: Int, newState: Int)
    }

    abstract fun close()
    abstract fun connect(device: BluetoothDevice)
    abstract fun disconnect()
    protected abstract fun getConnectionState(): Int

    fun isConnecting(): Boolean = mCurrentState == STATE_CONNECTING
    fun isConnected(): Boolean = mCurrentState == STATE_CONNECTED

    fun start() {
        mStopped = false
        mHandler.postDelayed(mHandlerRunnable, TRIGGER_INTERVAL)
    }

    fun stop() {
        mStopped = true
        mCurrentState = STATE_DISCONNECTED
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setDevice(device: BluetoothDevice) {
        mDevice = device
    }

    fun tryUpdateState(state: Int) {
        if (mStopped) {
            Log.d(TAG, "Stopped")
            return
        }
        Log.d(TAG, "${mHandlerThread.name}: ${getConnectionStateString(state)}")
        updateState(mCurrentState, state)
        mCurrentState = state
    }

    private fun updateState(oldState: Int, newState: Int) {
        mCallback?.onStateChanged(oldState, newState)
    }

    fun getCurrentState(): Int = mCurrentState
}
