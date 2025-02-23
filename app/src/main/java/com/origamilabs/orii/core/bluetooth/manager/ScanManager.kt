package com.origamilabs.orii.core.bluetooth.manager

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.util.Log
import com.origamilabs.orii.core.bluetooth.BluetoothHelper
import java.util.*

object ScanManager : BaseManager() {

    private const val SCAN_PERIOD = 30000L
    private const val TAG = "ScanManager"

    private var mListener: OnStateChangedListener? = null
    private var mCurrentState: State = State.NONE

    // Handler pour les tâches temporisées
    private val mHandler: Handler by lazy { Handler() }

    // Callback de scan Bluetooth LE
    private val mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "Scanned device: ${result.scanRecord?.deviceName}:${result.device.address}")
            if (BluetoothHelper.isOriiMacAddressInRange(result.device.address)) {
                mCurrentState = State.READY_TO_SCAN
                Log.d(TAG, "Found ORII")
                getBluetoothAdapter()?.bluetoothLeScanner?.stopScan(this)
                mListener?.onOriiFound(result.device)
            }
        }
    }

    interface OnStateChangedListener {
        fun onOriiFound(device: BluetoothDevice)
        fun onScanTimeout()
    }

    enum class State {
        READY_TO_SCAN,
        SCANNING,
        NONE
    }

    override fun onInitialize(): Boolean {
        mCurrentState = State.READY_TO_SCAN
        return true
    }

    override fun start() {
        if (mCurrentState == State.NONE) {
            throw RuntimeException("You have to initialize the ScanManager before calling start().")
        }
        val bondedOrii = findBondedOrii()
        if (bondedOrii == null) {
            mCurrentState = State.SCANNING
            scan()
        } else {
            mCurrentState = State.READY_TO_SCAN
            mListener?.onOriiFound(bondedOrii)
        }
    }

    override fun close() {
        stop()
    }

    fun stop() {
        getBluetoothAdapter()?.bluetoothLeScanner?.stopScan(mScanCallback)
    }

    fun setOnStateChangedListener(listener: OnStateChangedListener) {
        mListener = listener
    }

    private class ScanTimeoutRunnable : Runnable {
        override fun run() {
            if (mContext == null || ScanManager.mCurrentState != State.SCANNING) return
            mCurrentState = State.READY_TO_SCAN
            getBluetoothAdapter()?.bluetoothLeScanner?.stopScan(ScanManager.mScanCallback)
            mListener?.onScanTimeout()
        }
    }

    private fun scan() {
        mHandler.postDelayed(ScanTimeoutRunnable(), SCAN_PERIOD)
        getBluetoothAdapter()?.bluetoothLeScanner?.startScan(
            /* filters = */ null,
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build(),
            mScanCallback
        )
    }

    private fun findBondedOrii(): BluetoothDevice? {
        val bondedDevices = getBluetoothAdapter()?.bondedDevices ?: emptySet()
        Log.d(TAG, "Bonded Devices size: ${bondedDevices.size}")
        for (device in bondedDevices) {
            if (BluetoothHelper.isOriiMacAddressInRange(device.address)) {
                return device
            }
        }
        return null
    }
}
