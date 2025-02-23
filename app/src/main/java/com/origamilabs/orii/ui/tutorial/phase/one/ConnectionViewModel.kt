package com.origamilabs.orii.ui.tutorial.phase.one

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.core.bluetooth.manager.ScanManager
import com.origamilabs.orii.ui.main.MainActivity
import com.origamilabs.orii.ui.tutorial.phase.one.ConnectionFragment

class ConnectionViewModel : ViewModel(), ScanManager.OnStateChangedListener {

    companion object {
        private const val TAG = "ConnectionViewModel"
    }

    private var currentState: Int = 0
    private lateinit var fragment: ConnectionFragment

    // Callback pour surveiller les changements d'état d'Orii.
    private val mConnectionCallback: ConnectionManager.Callback = object : ConnectionManager.Callback {
        override fun onA2dpStateChange(prevState: Int, newState: Int) {
            // Implémentation vide
        }

        override fun onGattStateChange(prevState: Int, newState: Int) {
            // Implémentation vide
        }

        override fun onHeadsetStateChange(prevState: Int, newState: Int) {
            // Implémentation vide
        }

        override fun onOriiRemoveBond() {
            // Implémentation vide
        }

        override fun onOriiStateChange(prevState: Int, newState: Int) {
            if (newState != prevState) {
                currentState = newState
                if (currentState == 2) {
                    fragment.navigateToNext()
                }
            }
        }
    }

    fun getConnectionCallback(): ConnectionManager.Callback = mConnectionCallback

    fun addConnectionCallback() {
        ConnectionManager.getInstance().addCallback(getConnectionCallback())
    }

    fun removeConnectionCallback() {
        ConnectionManager.getInstance().removeCallback(getConnectionCallback())
    }

    fun init(fragment: ConnectionFragment) {
        this.fragment = fragment
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        removeConnectionCallback()
        super.onCleared()
    }

    fun getConnectionStateString(): String = when (currentState) {
        0 -> fragment.getString(R.string.tutorial_connection_state_disconnected)
        1 -> fragment.getString(R.string.tutorial_connection_state_connecting)
        2 -> fragment.getString(R.string.tutorial_connection_state_connected)
        else -> ""
    }

    fun scan() {
        ConnectionManager.getInstance().close()
        ScanManager.getInstance().setOnStateChangedListener(this)
        ScanManager.getInstance().start()
    }

    override fun onScanTimeout() {
        // Pas d'action particulière en cas de timeout
    }

    override fun onOriiFound(device: BluetoothDevice) {
        ConnectionManager.getInstance().start(device, mConnectionCallback)
        ConnectionManager.getInstance().setNotificationIntentClass(MainActivity::class.java)
    }
}
