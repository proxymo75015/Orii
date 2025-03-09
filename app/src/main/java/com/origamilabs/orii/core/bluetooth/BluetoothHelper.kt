package com.origamilabs.orii.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import timber.log.Timber

object BluetoothHelper {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothManager: BluetoothManager? = null

    private const val BEGIN_CVC_ADDRESS = "30:1F:9A:CF:FF:FF"
    private const val END_CVC_ADDRESS = "30:1F:9A:CF:FF:FF"
    private const val BEGIN_PI_TECH_ADDRESS = "F4:1F:9A:CF:FF:FF"
    private const val END_PI_TECH_ADDRESS = "F4:1F:9A:CF:FF:FF"
    private const val BEGIN_SECOND_ADDRESS = "00:02:5B:00:FF:01"

    /**
     * Vérifie si l'adresse MAC fournie se situe dans l'une des plages définies.
     */
    fun isOriiMacAddressInRange(address: String): Boolean {
        return (address.compareTo(BEGIN_PI_TECH_ADDRESS, ignoreCase = true) >= 0 &&
                address.compareTo(END_PI_TECH_ADDRESS, ignoreCase = true) <= 0) ||
                (address.compareTo(BEGIN_CVC_ADDRESS, ignoreCase = true) >= 0 &&
                        address.compareTo(END_CVC_ADDRESS, ignoreCase = true) <= 0) ||
                (address.compareTo(BEGIN_SECOND_ADDRESS, ignoreCase = true) == 0)
    }

    /**
     * Initialise le BluetoothManager et le BluetoothAdapter à partir du contexte.
     */
    private fun initBluetoothAdapter(context: Context) {
        if (mBluetoothManager == null) {
            mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            if (mBluetoothManager == null) {
                Timber.e("Impossible d'obtenir le BluetoothManager.")
                return
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager?.adapter
            if (mBluetoothAdapter == null) {
                Timber.e("Impossible d'obtenir le BluetoothAdapter.")
            }
        }
    }

    /**
     * Retourne le [BluetoothManager]. Si nécessaire, il est initialisé à partir du contexte.
     */
    fun getBluetoothManager(context: Context): BluetoothManager? {
        if (mBluetoothManager == null) {
            initBluetoothAdapter(context)
        }
        return mBluetoothManager
    }

    /**
     * Retourne le [BluetoothAdapter]. Si nécessaire, il est initialisé à partir du contexte.
     */
    fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        if (mBluetoothAdapter == null) {
            initBluetoothAdapter(context)
        }
        return mBluetoothAdapter
    }
}