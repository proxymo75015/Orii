package com.origamilabs.orii.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.HomeFragmentBinding
import com.origamilabs.orii.ui.main.ConnectionState
import com.origamilabs.orii.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        // Affecte le ViewModel au binding pour DataBinding (si nécessaire)
        binding.sharedViewModel = mainViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.welcomeText.text = getString(R.string.welcome_user, name)
        }

        mainViewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConnectionState.Disconnected -> binding.statusText.text = getString(R.string.status_disconnected)
                is ConnectionState.Connecting -> binding.statusText.text = getString(R.string.status_connecting)
                is ConnectionState.Connected -> binding.statusText.text = getString(R.string.status_connected)
                is ConnectionState.Error -> binding.statusText.text = getString(R.string.status_error, state.message)
            }
        }

        binding.connectButton.setOnClickListener {
            val oriiAddress = "00:11:22:33:44:55" // Remplacer par l'adresse réelle si besoin
            mainViewModel.connectToOrii(oriiAddress)
        }

        binding.disconnectButton.setOnClickListener {
            mainViewModel.disconnectOrii()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
