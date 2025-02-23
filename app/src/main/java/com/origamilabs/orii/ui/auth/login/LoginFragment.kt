package com.origamilabs.orii.ui.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.LoginFragmentBinding
import com.origamilabs.orii.ui.tutorial.TutorialActivity

class LoginFragment : Fragment() {

    // Utilisation du View Binding pour lier la vue
    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    // Récupération du ViewModel via le délégué viewModels()
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Vous pouvez utiliser ici LoginFragmentBinding.inflate si vous avez activé le ViewBinding
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // onActivityCreated est déprécié, nous utilisons donc onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // On informe le ViewModel de cette instance de fragment si nécessaire
        viewModel.setFragment(this)

        binding.signInButton.setOnClickListener {
            binding.isLoginProgress = true
            hideKeyboard()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            viewModel.handleLogin(email, password)
        }

        binding.forgotPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
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
            binding.isLoginProgress = false
        }
        binding.emailInputLayout.error = error
    }

    /**
     * Affiche une erreur sur le champ mot de passe.
     * Si un message d'erreur est fourni, l'indicateur de progression est désactivé.
     */
    fun setPasswordError(error: String?) {
        if (error != null) {
            binding.isLoginProgress = false
        }
        binding.passwordInputLayout.error = error
    }

    /**
     * Termine l'activité en cours et lance l'activité du tutoriel.
     */
    fun navigateToTutorial() {
        binding.isLoginProgress = false
        requireActivity().finish()
        Intent(requireContext(), TutorialActivity::class.java).apply {
            putExtra("exit_with_clear_task", true)
            startActivity(this)
        }
    }

    /**
     * Masque le clavier en récupérant le InputMethodManager.
     */
    fun hideKeyboard() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Permet d'éviter les fuites de mémoire
    }

    companion object {
        const val TAG = "LoginFragment"

        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}
