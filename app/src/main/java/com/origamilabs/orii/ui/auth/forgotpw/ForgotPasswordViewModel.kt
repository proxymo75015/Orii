package com.origamilabs.orii.ui.auth.forgotpw

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import com.origamilabs.orii.R
import com.origamilabs.orii.api.API

class ForgotPasswordViewModel : ViewModel() {

    fun handleForgotPassword(fragment: ForgotPasswordFragment, email: String) {
        if (email.isEmpty()) {
            fragment.setEmailError(fragment.getString(R.string.auth_forgot_password_error_email_empty))
            return
        }
        API.INSTANCE.forgotPassword(email, object : API.ResponseListener {
            override fun onSuccess(response: JsonObject) {
                Toast.makeText(fragment.activity, R.string.auth_forgot_password_success, Toast.LENGTH_SHORT).show()
                fragment.findNavController().navigateUp()
            }

            override fun onError(errorMessage: String) {
                when (errorMessage) {
                    "forgot_password_error_message" -> {
                        fragment.setEmailError(fragment.getString(R.string.auth_forgot_password_error_email_not_found))
                    }
                    "reset_time_within_24_hrs_more_than_3_times_message" -> {
                        fragment.setEmailError(fragment.getString(R.string.auth_forgot_password_error_24hrs_3times))
                    }
                }
            }
        })
    }
}
