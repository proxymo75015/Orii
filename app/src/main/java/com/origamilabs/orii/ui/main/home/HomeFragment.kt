package com.origamilabs.orii.ui.main.home

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
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.origamilabs.orii.Constants
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.databinding.HomeFragmentBinding
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.AppVersionInfo
import com.origamilabs.orii.models.User
import com.origamilabs.orii.ui.common.AppVersionInfoDialogFragment
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import com.origamilabs.orii.ui.main.SharedViewModel
import com.origamilabs.orii.ui.main.home.update.UpdateActivity
import com.origamilabs.orii.utils.SoundTester

class HomeFragment : Fragment() {

    private var binding: HomeFragmentBinding? = null
    private var homeViewModel: HomeViewModel? = null
    private var sharedViewModel: SharedViewModel? = null
    private var soundTester: SoundTester? = null

    // Receiver pour détecter la fin du téléchargement du firmware
    private val firmwareDownloadedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.unregisterReceiver(this)
            Log.d(TAG, "Firmware downloaded; update is now available")
            binding?.setCanFirmwareUpdate(true)
        }
    }

    // Receiver pour la réception des infos de version de l'app
    private val appVersionDownloadedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.unregisterReceiver(this)
            Log.d(TAG, "App version info downloaded; checking version")
            homeViewModel?.checkAppVersion()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding?.lifecycleOwner = viewLifecycleOwner

        // Initialisation du ViewModel propre au HomeFragment
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        // Observation du niveau de batterie
        homeViewModel?.batteryLevel?.observe(viewLifecycleOwner, Observer { level ->
            binding?.batteryLevelImageView?.setImageLevel(level ?: 0)
        })
        // Observation de l'info de version de l'app pour déclencher la création d'un dialogue
        homeViewModel?.appVersionInfo?.observe(viewLifecycleOwner, Observer { appVersionInfo: AppVersionInfo ->
            createAppVersionInfoDialog(
                appVersionInfo.language.common.newFeatures,
                appVersionInfo.language.common.bugFixes
            )
        })

        // Récupération du SharedViewModel de l'activité hôte
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
            binding?.sharedViewModel = sharedViewModel
        }

        // Initialisation du SoundTester avec un callback qui remet à jour le texte du bouton
        soundTester = SoundTester(requireContext()) {
            binding?.soundTestButton?.text = getString(R.string.home_sound_test_play)
        }
        binding?.soundTestButton?.setOnClickListener {
            Log.d(TAG, "Sound test button clicked")
            soundTester?.let { tester ->
                val newText = if (tester.toggleAudio()) {
                    getString(R.string.home_sound_test_stop)
                } else {
                    getString(R.string.home_sound_test_play)
                }
                binding?.soundTestButton?.text = newText
                AnalyticsManager.logSoundTest()
            }
        }

        // Mise à jour du texte d'accueil
        binding?.welcomeTextView?.text = getString(R.string.home_welcome, AppManager.currentUser?.name)

        // Mise à jour du texte de la version du firmware
        val firmwareText = getString(
            R.string.help_firmware,
            if (AppManager.currentFirmwareVersion == -1) "N/A" else AppManager.currentFirmwareVersion.toString()
        )
        binding?.firmwareTextView?.text = firmwareText

        // Bouton de réessai en cas de timeout du scan
        binding?.timeOutRetryButton?.setOnClickListener {
            sharedViewModel?.retryConnectOrii(getOriiFoundCallback(), getScanTimeoutCallback())
            AnalyticsManager.logBleRetry()
        }

        // Bouton d'arrêt de la recherche
        binding?.stopSearchingButton?.setOnClickListener {
            sharedViewModel?.stopSearchingOrii()
        }

        // Bouton de mise à jour du firmware
        binding?.updateButton?.setOnClickListener {
            sharedViewModel?.setAutoScan(false)
            AppManager.canFirmwareUpdate = false
            startActivity(Intent(context, UpdateActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel?.setAutoScan(true)
        binding?.setCanFirmwareUpdate(AppManager.canFirmwareUpdate)

        // Affiche une boîte de dialogue pour activer le GPS/Bluetooth si nécessaire
        BtLocationEnableDialogFragment.newInstance()?.apply {
            setOnDialogDismissListener { /* Callback à définir si besoin */ }
            fragmentManager?.let { show(it, "enable_bt_gps") }
        }

        // Si ORII est connecté, réinitialise le flag de vérification de la version du firmware
        if (ConnectionManager.getInstance().isOriiConnected()) {
            AppManager.firmwareVersionChecked = false
        }

        try {
            context?.registerReceiver(
                firmwareDownloadedReceiver,
                IntentFilter(Constants.FIRMWARE_DOWNLOADED_BROADCAST)
            )
            context?.registerReceiver(
                appVersionDownloadedReceiver,
                IntentFilter(Constants.APP_VERSION_INFO_DOWNLOADED_BROADCAST)
            )
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "The BroadcastReceiver is already registered")
        }
    }

    override fun onDetach() {
        super.onDetach()
        soundTester?.stopAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            context?.unregisterReceiver(firmwareDownloadedReceiver)
            context?.unregisterReceiver(appVersionDownloadedReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "The BroadcastReceiver is already unregistered")
        }
        Log.d(TAG, "onDestroy")
    }

    private fun createAppVersionInfoDialog(newFeatures: String, bugFixes: String) {
        val dialogFragment = AppVersionInfoDialogFragment.newInstance()
        dialogFragment?.apply {
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
            fragmentManager?.let { show(it, "app_version_info") }
        }
    }

    private fun openGooglePlay() {
        val packageName = context?.packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    // Ces callbacks sont ici définis de manière "vide" (no-op) ; ils pourront être ajustés selon la logique métier.
    private fun getScanTimeoutCallback(): () -> Unit = { /* no-op */ }
    private fun getOriiFoundCallback(): () -> Unit = { /* no-op */ }

    companion object {
        private const val TAG = "HomeFragment"
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
