package com.origamilabs.orii.ui.main.help.ui.help

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.origamilabs.orii.Constants
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.databinding.FirmwareTestFragmentBinding
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.AppVersionInfo
import com.origamilabs.orii.ui.MainApplication
import com.origamilabs.orii.ui.common.AppVersionInfoDialogFragment
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import com.origamilabs.orii.ui.SharedViewModel
import com.origamilabs.orii.ui.main.home.update.UpdateActivity

class FirmwareTestFragment : Fragment() {

    companion object {
        private const val TAG = "FirmwareTestFragment"
        fun newInstance(): FirmwareTestFragment = FirmwareTestFragment()
    }

    // Liaison générée par DataBinding (correspond à firmware_test_fragment.xml)
    private lateinit var binding: FirmwareTestFragmentBinding

    // ViewModels propres au fragment et partagé avec l'activité
    private lateinit var firmwareTestViewModel: FirmwareTestViewModel
    private lateinit var sharedViewModel: SharedViewModel

    // BroadcastReceiver pour détecter le téléchargement forcé du firmware
    private val firmwareForceDownloadedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.unregisterReceiver(this)
            Log.d(TAG, "can update, firmware downloaded")
            binding.canFirmwareForceUpdate = true
        }
    }

    // BroadcastReceiver pour détecter le téléchargement des informations de version de l'application
    private val appVersionDownloadedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.unregisterReceiver(this)
            Log.d(TAG, "appInfo downloaded, check app version is latest")
            firmwareTestViewModel.checkAppVersion()
        }
    }

    // Callbacks à passer pour le scan Bluetooth (actuellement vides, à personnaliser si besoin)
    private val scanTimeoutCallback: () -> Unit = { /* Implémentation à ajouter si nécessaire */ }
    private val oriiFoundCallback: () -> Unit = { /* Implémentation à ajouter si nécessaire */ }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Utilisation de DataBinding pour gonfler le layout
        binding = DataBindingUtil.inflate(inflater, R.layout.firmware_test_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        binding.lifecycleOwner = viewLifecycleOwner

        // Initialisation du ViewModel propre au fragment
        firmwareTestViewModel = ViewModelProvider(this).get(FirmwareTestViewModel::class.java)
        firmwareTestViewModel.appVersionInfo.observe(viewLifecycleOwner, Observer { appVersionInfo: AppVersionInfo ->
            createAppVersionInfoDialog(
                appVersionInfo.language.common.newFeatures,
                appVersionInfo.language.common.bugFixes
            )
        })

        // Récupération du ViewModel partagé depuis l'activité
        val activity = requireActivity()
        sharedViewModel = ViewModelProvider(activity).get(SharedViewModel::class.java)
        binding.sharedViewModel = sharedViewModel

        // Configuration du texte de bienvenue
        val welcomeTextView = binding.root.findViewById<TextView>(R.id.welcome_text_view)
        welcomeTextView.text = getString(R.string.firmware_test_mode_welcome)

        // Configuration des écouteurs des boutons
        binding.root.findViewById<Button>(R.id.timeOutRetryButton)?.setOnClickListener {
            sharedViewModel.retryConnectOrii(getOriiFoundCallback(), getScanTimeoutCallback())
            AnalyticsManager.logBleRetry()
        }
        binding.root.findViewById<Button>(R.id.stop_searching_button)?.setOnClickListener {
            sharedViewModel.stopSearchingOrii()
        }
        binding.root.findViewById<Button>(R.id.v68_button)?.setOnClickListener {
            MainApplication.instance.forceUpdateFirmware(68)
        }
        binding.root.findViewById<Button>(R.id.v69_button)?.setOnClickListener {
            MainApplication.instance.forceUpdateFirmware(69)
        }
        binding.root.findViewById<Button>(R.id.v70_button)?.setOnClickListener {
            MainApplication.instance.forceUpdateFirmware(70)
        }
        binding.root.findViewById<Button>(R.id.v71_button)?.setOnClickListener {
            MainApplication.instance.forceUpdateFirmware(71)
        }
        binding.root.findViewById<Button>(R.id.update_button)?.setOnClickListener {
            sharedViewModel.autoScan = false
            AppManager.setCanFirmwareForceUpdate(false)
            startActivity(Intent(context, UpdateActivity::class.java))
        }
    }

    /**
     * Crée et affiche une boîte de dialogue présentant les informations de version de l'application.
     *
     * @param newFeatures Les nouveautés à afficher.
     * @param bugFixes Les corrections de bugs à afficher.
     */
    fun createAppVersionInfoDialog(newFeatures: String, bugFixes: String) {
        val dialog = AppVersionInfoDialogFragment.newInstance()?.apply {
            setDialogNewFeaturesText(newFeatures)
            setDialogBugFixesText(bugFixes)
            setOnDialogClickListener(object : AppVersionInfoDialogFragment.OnDialogClickListener {
                override fun onDialogUpdateClick() {
                    openGooglePlay()
                    dismiss()
                }

                override fun onDialogCancelClick() {
                    dismiss()
                }
            })
        }
        dialog?.show(requireFragmentManager(), "app_version_info")
    }

    /**
     * Ouvre la page de l'application sur Google Play.
     */
    fun openGooglePlay() {
        val packageName = context?.packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        sharedViewModel.autoScan = true
        binding.canFirmwareForceUpdate = AppManager.canFirmwareForceUpdate

        // Affichage du dialogue pour activer Bluetooth et GPS
        val btLocationDialog = BtLocationEnableDialogFragment.newInstance()
        btLocationDialog?.setOnDialogDismissListener(BtLocationEnableDialogDismissListener(this))
        btLocationDialog?.show(requireFragmentManager(), "enable_bt_gps")

        if (ConnectionManager.getInstance().isOriiConnected()) {
            AppManager.setFirmwareVersionChecked(false)
        }

        // Enregistrement des BroadcastReceivers
        try {
            context?.registerReceiver(
                firmwareForceDownloadedBroadcastReceiver,
                IntentFilter(Constants.FIRMWARE_FORCE_DOWNLOADED_BROADCAST)
            )
            context?.registerReceiver(
                appVersionDownloadedBroadcastReceiver,
                IntentFilter(Constants.APP_VERSION_INFO_DOWNLOADED_BROADCAST)
            )
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "The BroadcastReceiver is already registered")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            context?.unregisterReceiver(firmwareForceDownloadedBroadcastReceiver)
            context?.unregisterReceiver(appVersionDownloadedBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "The BroadcastReceiver is already unregistered")
        }
        Log.d(TAG, "onDestroy")
    }

    fun getScanTimeoutCallback(): () -> Unit = scanTimeoutCallback
    fun getOriiFoundCallback(): () -> Unit = oriiFoundCallback

    /**
     * Implémentation du listener déclenché à la fermeture du dialogue d'activation
     * Bluetooth et GPS.
     */
    private class BtLocationEnableDialogDismissListener(val fragment: FirmwareTestFragment) :
        BtLocationEnableDialogFragment.OnDialogDismissListener {
        override fun onDialogDismiss(btEnabled: Boolean, gpsEnabled: Boolean) {
            if (btEnabled && gpsEnabled) {
                Log.d(TAG, "Enabled Bluetooth and Location/GPS")
                MainApplication.instance.bindBluetoothService {
                    fragment.firmwareTestViewModel.addServiceListener()
                    if (!ConnectionManager.getInstance().isOriiConnected()) {
                        fragment.sharedViewModel.scan(
                            fragment.getOriiFoundCallback(),
                            fragment.getScanTimeoutCallback()
                        )
                    }
                }
            } else {
                Log.d(TAG, "Bluetooth and Location/GPS not enabled")
                (fragment.activity as? MainActivity)?.changeTab(1)
            }
        }

        companion object {
            private const val TAG = "FirmwareTestFragment"
        }
    }
}
