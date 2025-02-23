package com.origamilabs.orii.ui.auth.forgotpw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.ForgotPasswordFragmentBinding

class ForgotPasswordFragment : Fragment() {

    private var _binding: ForgotPasswordFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ForgotPasswordViewModel

    companion object {
        fun newInstance() = ForgotPasswordFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ForgotPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.sendButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            viewModel.handleForgotPassword(this, email)
        }
    }

    /**
     * Affiche une erreur sur le champ email.
     *
     * @param msg Le message d'erreur à afficher.
     */
    fun setEmailError(msg: String) {
        binding.emailInput.error = msg
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
