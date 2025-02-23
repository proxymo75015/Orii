package com.origamilabs.orii.ui.tutorial.phase.one

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.ui.MainApplication
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import kotlin.Unit
import kotlin.jvm.functions.Function0

class ConnectionFragmentShowDialogListener(
    private val connectionFragment: ConnectionFragment,
    private val dialog: BtLocationEnableDialogFragment
) : BtLocationEnableDialogFragment.OnDialogDismissListener {

    override fun onDialogDismiss(btEnabled: Boolean, gpsEnabled: Boolean) {
        if (btEnabled && gpsEnabled) {
            Log.d("ConnectionFragment", "Enabled Bluetooth and Location/GPS")
            MainApplication.instance.getInstance().bindBluetoothService {
                val connectionManager = ConnectionManager.getInstance()
                if (!connectionManager.isOriiConnected()) {
                    connectionFragment.viewModel.scan()
                }
                Unit
            }
        } else {
            Log.d("ConnectionFragment", "Bluetooth and Location/GPS not enabled")
            Toast.makeText(
                connectionFragment.context,
                connectionFragment.getString(R.string.bt_n_location_dialog_not_enabled),
                Toast.LENGTH_SHORT
            ).show()
            val fragmentManager: FragmentManager = connectionFragment.fragmentManager
                ?: throw NullPointerException("FragmentManager is null")
            dialog.show(fragmentManager, "enable_bt_gps")
        }
    }
}
