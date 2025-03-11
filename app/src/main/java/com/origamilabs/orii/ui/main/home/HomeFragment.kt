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
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.origamilabs.orii.Constants
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.databinding.HomeFragmentBinding
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.AppVersionInfo
import com.origamilabs.orii.ui.HomeViewModel
import com.origamilabs.orii.ui.common.AppVersionInfoDialogFragment
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import com.origamilabs.orii.ui.SharedViewModel
import com.origamilabs.orii.ui.main.home.update.UpdateActivity
import com.origamilabs.orii.utils.SoundTester
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    // HomeViewModel est fourni par Hilt via le delegate by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    // SharedViewModel injecté depuis l’Activity
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // ConnectionManager injecté via Hilt
    @Inject
    lateinit var connectionManager: ConnectionManager

    private var soundTester: SoundTester? = null

    // Receiver pour la fin du téléchargement du firmware
    private val firmwareDownloadedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Firmware downloaded; update is now available")
            // Utilisation explicite de la méthode setter du DataBinding
            binding.setCanFirmwareUpdate(true)
        }
    }

    // Receiver pour la réception des infos de version de l'app
    private val appVersionDownloadedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "App version info downloaded; checking version")
            homeViewModel.checkAppVersion()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundTester = SoundTester(requireContext()) {
            binding.soundTestButton.text = getString(R.string.home_sound_test_play)
        }
        binding.soundTestButton.setOnClickListener {
            soundTester?.let { tester ->
                val newText = if (tester.toggleAudio()) {
                    getString(R.string.home_sound_test_stop)
                } else {
                    getString(R.string.home_sound_test_play)
                }
                binding.soundTestButton.text = newText
                AnalyticsManager.logSoundTest()
            }
        }

        // Affichage du message de bienvenue
        binding.welcomeTextView.text = getString(
            R.string.home_welcome,
            AppManager.getCurrentUser()?.name
        )

        // Mise à jour de la version du firmware
        val firmwareVersion = AppManager.getFirmwareVersion()
        val firmwareText = getString(
            R.string.help_firmware,
            if (firmwareVersion == -1) "N/A" else firmwareVersion.toString()
        )
        // On tente d'utiliser le binding généré ; sinon, on récupère la vue par findViewById
        val fwTextView: TextView = try {
            binding.firmwareTextView
        } catch (e: Exception) {
            view.findViewById(R.id.firmwareTextView)
        }
        fwTextView.text = firmwareText

        // Bouton retry : lancement d'un nouveau scan
        binding.timeOutRetryButton.setOnClickListener {
            sharedViewModel.scan(getOriiFoundCallback(), getScanTimeoutCallback())
            AnalyticsManager.logBleRetry()
        }

        // Bouton stop
        binding.stopSearchingButton.setOnClickListener { sharedViewModel.stopSearchingOrii() }

        // Bouton update
        binding.updateButton.setOnClickListener {
            sharedViewModel.autoScan = false
            AppManager.setCanFirmwareUpdate(false)
            startActivity(Intent(requireContext(), UpdateActivity::class.java))
        }

        // Observation du niveau de batterie
        homeViewModel.batteryLevel.observe(viewLifecycleOwner) { level ->
            binding.batteryLevelImageView.setImageLevel(level ?: 0)
        }
        // Observation des infos de version de l'app pour afficher la boîte de dialogue
        homeViewModel.appVersionInfo.observe(viewLifecycleOwner) { appVersionInfo: AppVersionInfo ->
            createAppVersionInfoDialog(
                appVersionInfo.language.common.newFeatures,
                appVersionInfo.language.common.bugFixes
            )
        }
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.autoScan = true
        binding.setCanFirmwareUpdate(AppManager.getCanFirmwareUpdate())

        // Affichage du dialogue pour activer Bluetooth/GPS
        BtLocationEnableDialogFragment.newInstance()?.apply {
            setOnDialogDismissListener(object : BtLocationEnableDialogFragment.OnDialogDismissListener {
                override fun onDialogDismiss(btEnabled: Boolean, gpsEnabled: Boolean) {
                    // Vous pouvez traiter ici les états si besoin
                }
            })
            show(parentFragmentManager, "enable_bt_gps")
        }

        if (connectionManager.isOriiConnected()) {
            AppManager.setFirmwareVersionChecked(false)
        }

        // Enregistrement des BroadcastReceiver en passant le flag RECEIVER_NOT_EXPORTED
        requireContext().registerReceiver(
            firmwareDownloadedReceiver,
            IntentFilter(Constants.FIRMWARE_DOWNLOADED_BROADCAST),
            Context.RECEIVER_NOT_EXPORTED,
            null
        )
        requireContext().registerReceiver(
            appVersionDownloadedReceiver,
            IntentFilter(Constants.APP_VERSION_INFO_DOWNLOADED_BROADCAST),
            Context.RECEIVER_NOT_EXPORTED,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(firmwareDownloadedReceiver)
            requireContext().unregisterReceiver(appVersionDownloadedReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "BroadcastReceiver déjà désenregistré")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundTester?.stopAudio()
        _binding = null
    }

    private fun createAppVersionInfoDialog(newFeatures: String, bugFixes: String) {
        AppVersionInfoDialogFragment.newInstance()?.let { dialog ->
            dialog.dialogNewFeaturesText = newFeatures
            dialog.dialogBugFixesText = bugFixes
            dialog.setOnDialogClickListener(object : AppVersionInfoDialogFragment.OnDialogClickListener {
                override fun onDialogUpdateClick() {
                    openGooglePlay()
                    dialog.dismiss()
                }
                override fun onDialogCancelClick() {
                    dialog.dismiss()
                }
            })
            dialog.show(parentFragmentManager, "app_version_info")
        }
    }

    private fun openGooglePlay() {
        val packageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun getScanTimeoutCallback(): () -> Unit = { /* no-op */ }
    private fun getOriiFoundCallback(): () -> Unit = { /* no-op */ }

    companion object {
        const val TAG = "HomeFragment"
        fun newInstance() = HomeFragment()
    }
}
