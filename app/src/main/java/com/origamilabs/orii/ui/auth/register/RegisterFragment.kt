package com.origamilabs.orii.ui.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.RegisterFragmentBinding

class RegisterFragment : Fragment() {

    // Utilisation du View Binding pour accéder aux vues
    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!

    // Récupération du ViewModel via le délégué viewModels()
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // onActivityCreated étant déprécié, nous utilisons onViewCreated pour configurer les vues
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setFragment(this)

        binding.signUpButton.setOnClickListener {
            binding.isSignUpProgress = true
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()
            viewModel.handleRegister(email, password, confirmPassword)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * Affiche une erreur sur le champ email.
     * Si un message d'erreur est fourni, l'indicateur de progression est désactivé.
     */
    fun setEmailError(error: String?) {
        if (error != null) {
            binding.isSignUpProgress = false
        }
        binding.emailInputLayout.error = error
    }

    /**
     * Affiche une erreur sur le champ mot de passe.
     * Si un message d'erreur est fourni, l'indicateur de progression est désactivé.
     */
    fun setPasswordError(error: String?) {
        if (error != null) {
            binding.isSignUpProgress = false
        }
        binding.passwordInputLayout.error = error
    }

    /**
     * Affiche une erreur sur le champ de confirmation du mot de passe.
     * Si un message d'erreur est fourni, l'indicateur de progression est désactivé.
     */
    fun setConfirmPasswordError(error: String?) {
        if (error != null) {
            binding.isSignUpProgress = false
        }
        binding.confirmPasswordInputLayout.error = error
    }

    /**
     * Navigue vers l'écran de mail envoyé après l'inscription.
     */
    fun navigateToMailSent() {
        binding.isSignUpProgress = false
        findNavController().navigate(R.id.action_registerFragment_to_mailSentFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Permet d'éviter les fuites de mémoire
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}
