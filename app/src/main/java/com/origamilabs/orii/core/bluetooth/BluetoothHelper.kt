package com.origamilabs.orii.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

object BluetoothHelper {
    const val REQUEST_CODE_BLUETOOTH_CONNECT = 1001

    private const val TAG = "BluetoothHelper"
    private const val beginCvcAddress = "30:1F:9A:C0:00:00"
    private const val beginPiTechAddress = "F4:45:ED:21:90:1F"
    private const val beginSecondAddress = "00:02:5B:00:FF:01"
    private const val endCvcAddress = "30:1F:9A:CF:FF:FF"
    private const val endPiTechAddress = "F4:45:ED:21:90:82"
    private const val endSecondAddress = "00:02:5B:00:FF:05"

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothManager: BluetoothManager? = null

    /**
     * Vérifie si l'adresse MAC fournie se situe dans l'une des plages définies.
     */
    fun isOriiMacAddressInRange(address: String): Boolean {
        return (address.compareTo(beginPiTechAddress, ignoreCase = true) >= 0 &&
                address.compareTo(endPiTechAddress, ignoreCase = true) <= 0)
                || (address.compareTo(beginSecondAddress, ignoreCase = true) >= 0 &&
                address.compareTo(endSecondAddress, ignoreCase = true) <= 0)
                || (address.compareTo(beginCvcAddress, ignoreCase = true) >= 0 &&
                address.compareTo(endCvcAddress, ignoreCase = true) <= 0)
    }

    /**
     * Initialise le BluetoothManager et le BluetoothAdapter à partir du contexte.
     */
    private fun initBluetoothAdapter(context: Context) {
        if (mBluetoothManager == null) {
            mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
            }
        }
        mBluetoothAdapter = mBluetoothManager?.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
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
        if (mBluetoothManager == null || mBluetoothAdapter == null) {
            initBluetoothAdapter(context)
        }
        return mBluetoothAdapter
    }

    /**
     * Obtient le proxy pour le profil Bluetooth spécifié.
     * Retourne vrai si le proxy est obtenu, sinon faux.
     */
    fun getProfileProxy(context: Context, serviceListener: BluetoothProfile.ServiceListener, profile: Int): Boolean {
        return getBluetoothAdapter(context)?.getProfileProxy(context, serviceListener, profile) ?: false
    }
}
