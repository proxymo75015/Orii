package com.origamilabs.orii.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.facebook.FacebookSdk
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.AuthFragmentBinding
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.ui.tutorial.TutorialActivity

class AuthFragment : Fragment() {

    private var _binding: AuthFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    companion object {
        private const val TAG = "AuthFragment"
        fun newInstance() = AuthFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = AuthFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        viewModel.init(this)

        binding.emailLoginButton.setOnClickListener {
            val activity = requireActivity()
            val navController = Navigation.findNavController(activity, R.id.fragment)
            navController.navigate(R.id.action_authFragment_to_loginFragment)
        }

        binding.fbLoginButton.setOnClickListener {
            binding.isLoginProgress = true
            viewModel.handleFbLogin()
        }

        binding.googleLoginButton.setOnClickListener {
            binding.isLoginProgress = true
            viewModel.handleGoogleLogin()
        }

        binding.signUpTextView.setOnClickListener {
            val activity = requireActivity()
            val navController = Navigation.findNavController(activity, R.id.fragment)
            navController.navigate(R.id.action_authFragment_to_registerFragment)
        }

        val userId = arguments?.getString("userId", "") ?: ""
        val verifyCode = arguments?.getString("verifyCode", "") ?: ""
        if (userId.isNotEmpty() && verifyCode.isNotEmpty()) {
            if (AppManager.instance.currentUser != null) {
                Snackbar.make(
                    binding.authMainLayout,
                    R.string.auth_login_error_already_verified,
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                viewModel.handleLoginWithVerify(userId, verifyCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.isLoginProgress = false
        if (requestCode != 3) {
            if (FacebookSdk.isFacebookRequestCode(requestCode)) {
                viewModel.setFbLoginActivityResult(requestCode, resultCode, data)
            }
        } else {
            if (resultCode == -1) {
                try {
                    val result = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException::class.java)
                    viewModel.firebaseAuthWithGoogle(result)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }
    }

    fun setGoogleLoginError() {
        Snackbar.make(
            binding.authMainLayout,
            getString(R.string.auth_login_fail_google),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun setFacebookLoginError() {
        Snackbar.make(
            binding.authMainLayout,
            getString(R.string.auth_login_fail_facebook),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun navigateToTutorial() {
        Log.d(TAG, "activity: $activity")
        val intent = Intent(activity, TutorialActivity::class.java)
        requireActivity().finish()
        intent.putExtra("exit_with_clear_task", true)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
