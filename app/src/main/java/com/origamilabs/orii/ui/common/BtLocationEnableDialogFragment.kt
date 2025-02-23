package com.origamilabs.orii.ui.common

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.origamilabs.orii.R

class BtLocationEnableDialogFragment : DialogFragment() {

    // Indique si le Bluetooth et le GPS sont activés
    private var btEnabled: Boolean = false
    private var gpsEnabled: Boolean = false

    // Références aux Switch UI
    private var btSwitch: Switch? = null
    private var gpsSwitch: Switch? = null

    // Services et outils système
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mLocationManager: LocationManager
    private lateinit var mHandler: Handler

    // Listener de dismissal de la dialog
    private var mListener: OnDialogDismissListener? = null

    interface OnDialogDismissListener {
        fun onDialogDismiss(btEnabled: Boolean, gpsEnabled: Boolean)
    }

    fun setOnDialogDismissListener(listener: OnDialogDismissListener) {
        mListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mHandler = Handler()
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        val inflater = activity?.layoutInflater ?: LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.bt_location_enable_dialog_fragment, null)

        // Initialisation des Switch
        btSwitch = view.findViewById(R.id.bt_switch)
        gpsSwitch = view.findViewById(R.id.gps_switch)

        // Configuration du Switch Bluetooth
        btSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (btEnabled) {
                    btSwitch?.isEnabled = false
                } else {
                    btEnabled = mBluetoothAdapter.enable()
                }
                btSwitch?.isEnabled = true
                mHandler.postDelayed({
                    if (btEnabled && gpsEnabled) {
                        try {
                            dismiss()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                    }
                }, 1000L)
            }
        }

        // Configuration du Switch GPS
        gpsSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!gpsEnabled) {
                    startActivity(Intent("android.settings.LOCATION_SOURCE_SETTINGS"))
                }
                gpsSwitch?.isEnabled = false
                gpsEnabled = true
            }
        }

        builder.setView(view)
        builder.setNegativeButton(getString(R.string.bt_n_location_dialog_later)) { _, _ ->
            dismiss()
        }
        isCancelable = false
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        // Vérifie l'état actuel du Bluetooth et du GPS
        btEnabled = mBluetoothAdapter.isEnabled
        gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (btEnabled && gpsEnabled) {
            dismiss()
        }
        setBtEnabled(btEnabled)
        setGpsEnabled(gpsEnabled)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        shown = false
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        shown = false
        mListener?.onDialogDismiss(btEnabled, gpsEnabled)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = requireActivity()
        mBluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        mLocationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    // Met à jour l'état du Switch Bluetooth
    private fun setBtEnabled(enabled: Boolean) {
        btSwitch?.isChecked = enabled
        if (!enabled) {
            btSwitch?.isEnabled = true
        }
    }

    // Met à jour l'état du Switch GPS
    private fun setGpsEnabled(enabled: Boolean) {
        gpsSwitch?.isChecked = enabled
        if (!enabled) {
            gpsSwitch?.isEnabled = true
        }
    }

    companion object {
        // Variable pour éviter d'afficher la dialog plusieurs fois
        private var shown = false

        fun newInstance(): BtLocationEnableDialogFragment? {
            return if (shown) {
                null
            } else {
                shown = true
                BtLocationEnableDialogFragment()
            }
        }
    }
}
