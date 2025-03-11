package com.origamilabs.orii.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import com.origamilabs.orii.ui.main.home.update.UpdateActivity
import com.origamilabs.orii.utils.SoundTester
import com.origamilabs.orii.Constants
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.databinding.HomeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    @Inject
    lateinit var connectionManager: ConnectionManager

    private val soundTester: SoundTester by lazy {
        SoundTester(requireContext()) {
            binding.soundTestButton.text = homeViewModel.soundTestPlayText
        }
    }

    private val firmwareDownloadedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.d("Firmware téléchargé")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            homeViewModel.batteryLevel.observe(viewLifecycleOwner) { level ->
                batteryLevelImageView.setImageLevel(level ?: 0)
            }

            sharedViewModel = this@HomeFragment.sharedViewModel

            soundTestButton.setOnClickListener {
                Timber.d("Sound test button clicked")
                val isPlaying = soundTester.toggleAudio()
                soundTestButton.text = if (isPlaying)
                    homeViewModel.soundTestStopText
                else
                    homeViewModel.soundTestPlayText
                Timber.d("Sound test ${if (isPlaying) "started" else "stopped"}")
            }

            welcomeTextView.text = homeViewModel.welcomeMessage

            firmwareVersionTextView.text = homeViewModel.getFirmwareText()

            timeOutRetryButton.setOnClickListener {
                sharedViewModel.retryConnectOrii()
                Timber.d("Retry connect button clicked")
            }

            stopSearchingButton.setOnClickListener {
                sharedViewModel.stopSearchingOrii()
                Timber.d("Stop searching button clicked")
            }

            updateButton.setOnClickListener {
                sharedViewModel.setAutoScan(false)
                AppManager.setCanFirmwareUpdate(false)
                startActivity(Intent(requireContext(), UpdateActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.setAutoScan(true)
        binding.canFirmwareUpdate = AppManager.getCanFirmwareUpdate()

        BtLocationEnableDialogFragment.newInstance()?.apply {
            setOnDialogDismissListener(object : BtLocationEnableDialogFragment.OnDialogDismissListener {
                override fun onDialogDismiss(btEnabled: Boolean, gpsEnabled: Boolean) {}
            })
            show(parentFragmentManager, "enable_bt_gps")
        }

        if (connectionManager.isOriiConnected()) {
            AppManager.setFirmwareVersionChecked(false)
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            firmwareDownloadedReceiver,
            IntentFilter(Constants.FIRMWARE_DOWNLOADED_BROADCAST)
        )
    }

    override fun onPause() {
        super.onPause()
        try {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(firmwareDownloadedReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.d("BroadcastReceiver déjà désenregistré")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundTester.stopAudio()
        _binding = null
    }
}
