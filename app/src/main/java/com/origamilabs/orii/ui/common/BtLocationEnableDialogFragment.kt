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

/**
 * DialogFragment invitant l'utilisateur à activer le Bluetooth et le GPS.
 * Les textes et commentaires sont en français, le code en anglais.
 */
class BtLocationEnableDialogFragment : DialogFragment() {

    private var btEnabled: Boolean = false
    private var gpsEnabled: Boolean = false

    private var btSwitch: Switch? = null
    private var gpsSwitch: Switch? = null

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mLocationManager: LocationManager
    private lateinit var mHandler: Handler

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

        btSwitch = view.findViewById(R.id.bt_switch)
        gpsSwitch = view.findViewById(R.id.gps_switch)

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
                        dismiss()
                    }
                }, 1000L)
            }
        }

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
        mBluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        mLocationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun setBtEnabled(enabled: Boolean) {
        btSwitch?.isChecked = enabled
        if (!enabled) {
            btSwitch?.isEnabled = true
        }
    }

    private fun setGpsEnabled(enabled: Boolean) {
        gpsSwitch?.isChecked = enabled
        if (!enabled) {
            gpsSwitch?.isEnabled = true
        }
    }

    companion object {
        private var shown = false
        fun newInstance(): BtLocationEnableDialogFragment? {
            return if (shown) null else BtLocationEnableDialogFragment().also { shown = true }
        }
    }
}
