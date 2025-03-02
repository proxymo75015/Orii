package com.origamilabs.orii.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.origamilabs.orii.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observer le nom d’utilisateur local et afficher un message de bienvenue
        mainViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.welcomeText.text = getString(R.string.welcome_user, name)
        }

        // Observer l’état de connexion Bluetooth pour mettre à jour l’UI
        mainViewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConnectionState.Disconnected -> {
                    binding.statusText.text = getString(R.string.status_disconnected)
                }
                is ConnectionState.Connecting -> {
                    binding.statusText.text = getString(R.string.status_connecting)
                }
                is ConnectionState.Connected -> {
                    binding.statusText.text = getString(R.string.status_connected)
                }
                is ConnectionState.Error -> {
                    binding.statusText.text = getString(R.string.status_error, state.message)
                }
            }
        }

        // Bouton pour se connecter à la bague (adresse MAC à adapter ou sélectionner)
        binding.connectButton.setOnClickListener {
            // Par exemple, adresse MAC stockée/préférée
            val oriiAddress = "00:11:22:33:44:55"  // <== Remplacer par l’adresse réelle de la bague
            mainViewModel.connectToOrii(oriiAddress)
        }
        // Bouton pour se déconnecter
        binding.disconnectButton.setOnClickListener {
            mainViewModel.disconnectOrii()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
