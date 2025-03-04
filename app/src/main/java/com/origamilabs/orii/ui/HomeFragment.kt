package com.origamilabs.orii.ui.main.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.databinding.HomeFragmentBinding
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import com.origamilabs.orii.ui.main.SharedViewModel

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
        fun newInstance() = HomeFragment()
    }

    // Binding vers le layout home_fragment
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel propre au Home (MVVM pour données spécifiques)
    private lateinit var homeViewModel: HomeViewModel

    // ViewModel partagé avec l’activité principale
    private lateinit var sharedViewModel: SharedViewModel

    // Pour tester le son (haut-parleur Orii) – composant local
    private var soundTester: SoundTester? = null

    // Receiver pour détecter la fin du téléchargement du firmware (le cas échéant, local)
    private val firmwareDownloadedReceiver = object : androidx.localbroadcastmanager.content.LocalBroadcastManager? = null
    // Remarque: on peut utiliser LocalBroadcastManager pour émissions internes, ou LiveData via le ViewModel.

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = viewLifecycleOwner  // pour DataBinding avec LiveData
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialisation du HomeViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // Observation du niveau de batterie de la bague
        homeViewModel.batteryLevel.observe(viewLifecycleOwner) { level ->
            binding.batteryLevelImageView.setImageLevel(level ?: 0)
        }

        // Récupération du SharedViewModel de l’activité hôte (fourni par l’activité via Hilt)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        binding.sharedViewModel = sharedViewModel  // lie le ViewModel partagé au binding si on veut l’utiliser en XML

        // Configuration du SoundTester (lecture d'un son de test sur la bague)
        soundTester = SoundTester(requireContext()) {
            // Callback à la fin du test audio : réinitialise le texte du bouton
            binding.soundTestButton.text = getString(R.string.home_sound_test_play)
        }
        binding.soundTestButton.setOnClickListener {
            Log.d(TAG, "Sound test button clicked")
            soundTester?.let { tester ->
                val isPlaying = tester.toggleAudio()
                // Met à jour le texte du bouton en fonction de l’état lecture/arrêt
                binding.soundTestButton.text = if (isPlaying) {
                    getString(R.string.home_sound_test_stop)
                } else {
                    getString(R.string.home_sound_test_play)
                }
                // Journalise localement l’événement de test audio (plus d’envoi externe)
                Log.d(TAG, "Sound test ${if (isPlaying) "started" else "stopped"}")
            }
        }

        // Mise à jour du texte de bienvenue (plus de nom utilisateur car pas de login requis)
        binding.welcomeTextView.text = getString(R.string.home_welcome)  // ex: "Bienvenue !"

        // Mise à jour du texte de version du firmware de la bague connu (sinon N/A)
        val firmwareVersion = if (AppManager.currentFirmwareVersion == -1) "N/A"
        else AppManager.currentFirmwareVersion.toString()
        binding.firmwareTextView.text = getString(R.string.help_firmware, firmwareVersion)

        // Bouton "Réessayer" en cas d’échec/timeout de connexion -> relance la recherche
        binding.timeOutRetryButton.setOnClickListener {
            // Relance la procédure de connexion via le SharedViewModel
            sharedViewModel.retryConnectOrii()
            Log.d(TAG, "Retry connect button clicked")
        }

        // Bouton "Arrêter la recherche"
        binding.stopSearchingButton.setOnClickListener {
            sharedViewModel.stopSearchingOrii()
            Log.d(TAG, "Stop searching button clicked")
        }

        // Bouton de mise à jour du firmware (s’il y a une mise à jour disponible en local)
        binding.updateButton.setOnClickListener {
            // Lorsqu’on lance la mise à jour, on désactive l’auto-scan temporairement
            sharedViewModel.setAutoScan(false)
            AppManager.canFirmwareUpdate = false
            // Démarre l’activité de mise à jour du firmware (UpdateActivity)
            startActivity(Intent(requireContext(), UpdateActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Active la recherche automatique de la bague lorsque l’écran Home est visible
        sharedViewModel.setAutoScan(true)
        // Met à jour l’état du bouton de mise à jour en fonction de AppManager.canFirmwareUpdate
        binding.canFirmwareUpdate = AppManager.canFirmwareUpdate

        // Invite l’utilisateur à activer Bluetooth/GPS si nécessaire pour le scan BLE
        BtLocationEnableDialogFragment.newInstance()?.apply {
            setOnDialogDismissListener { /* peut implémenter une action après fermeture si besoin */ }
            show(parentFragmentManager, "enable_bt_gps")
        }

        // Si la bague est déjà connectée, on peut remettre à faux le flag de vérif de firmware
        if (ConnectionManager.getInstance().isOriiConnected()) {
            AppManager.firmwareVersionChecked = false
        }

        // Inscription du receiver local pour la fin de téléchargement firmware (si toujours utilisé)
        requireContext().registerReceiver(firmwareDownloadedReceiver,
            IntentFilter(Constants.FIRMWARE_DOWNLOADED_BROADCAST))
    }

    override fun onPause() {
        super.onPause()
        // Désenregistrement des receivers
        try {
            requireContext().unregisterReceiver(firmwareDownloadedReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "BroadcastReceiver déjà désenregistré")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Arrête le son de test si en cours
        soundTester?.stopAudio()
        _binding = null
    }

    // (Plus de méthodes getOriiFoundCallback() ou getScanTimeoutCallback() ici – la logique de connexion se fait dans le ViewModel)
}
