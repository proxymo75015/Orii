package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.origamilabs.orii.core.bluetooth.BluetoothService

abstract class BaseManager {

    companion object {
        private var mBluetoothAdapter: BluetoothAdapter? = null
        private var mBluetoothManager: BluetoothManager? = null
    }

    protected lateinit var mContext: Context
    protected var mBluetoothService: BluetoothService? = null
    private var mIsInitialized: Boolean = false

    abstract fun close()
    protected abstract fun onInitialize(): Boolean
    protected abstract fun start()

    fun isInitialized(): Boolean = mIsInitialized

    /**
     * Initialise le manager avec le contexte donné.
     *
     * Note : Ce code suppose que le [context] est aussi une instance de [BluetoothService].
     */
    fun initialize(context: Context): Boolean {
        mContext = context
        mBluetoothService = context as BluetoothService
        mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager?.adapter
        mIsInitialized = onInitialize()
        return mIsInitialized
    }

    protected fun getBluetoothManager(): BluetoothManager? {
        if (mBluetoothManager == null) {
            mBluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        }
        return mBluetoothManager
    }

    protected fun getBluetoothAdapter(): BluetoothAdapter? {
        if (mBluetoothManager != null) {
            if (mBluetoothAdapter == null) {
                mBluetoothAdapter = mBluetoothManager?.adapter
            }
        } else {
            mBluetoothAdapter = getBluetoothManager()?.adapter
        }
        return mBluetoothAdapter
    }
}
