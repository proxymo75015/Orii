package com.origamilabs.orii.ui.auth.register

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.origamilabs.orii.R
import com.origamilabs.orii.api.API
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.utils.Validator

class RegisterViewModel : ViewModel() {

    // Le fragment doit être initialisé avant l'appel de handleRegister
    lateinit var fragment: RegisterFragment

    fun setFragment(fragment: RegisterFragment) {
        this.fragment = fragment
    }

    fun handleRegister(email: String, password: String, confirmPassword: String) {
        // Réinitialiser les messages d'erreur
        fragment.setEmailError(null)
        fragment.setPasswordError(null)
        fragment.setConfirmPasswordError(null)

        // Validation de l'email
        if (email.isEmpty()) {
            fragment.setEmailError(fragment.getString(R.string.auth_error_email_is_empty))
            return
        }
        if (!Validator.isEmailValid(email)) {
            fragment.setEmailError(fragment.getString(R.string.auth_error_invalid_email_format))
            return
        }
        // Validation du mot de passe
        if (password.isEmpty() || password.length < 8) {
            fragment.setPasswordError(fragment.getString(R.string.auth_error_invalid_password_format))
            return
        }
        // Vérification que le mot de passe et sa confirmation correspondent
        if (password != confirmPassword) {
            fragment.setConfirmPasswordError(
                fragment.getString(R.string.auth_register_error_confirm_password_not_match)
            )
            return
        }

        // Appel à l'API pour enregistrer l'utilisateur
        API.register(email, password, AppManager.getUuid(), object : API.ResponseListener {
            override fun onSuccess(response: JsonObject) {
                fragment.navigateToMailSent()
            }

            override fun onError(errorMessage: String) {
                when (errorMessage) {
                    "invalid_password_length_message" ->
                        fragment.setPasswordError(
                            fragment.getString(R.string.auth_register_error_invalid_password_length)
                        )
                    "invalid_email_format_message" ->
                        fragment.setEmailError(
                            fragment.getString(R.string.auth_register_error_invalid_email_format)
                        )
                    "email_exist_message" ->
                        fragment.setEmailError(
                            fragment.getString(R.string.auth_register_error_email_exist)
                        )
                    "email_not_real_message" ->
                        fragment.setEmailError(
                            fragment.getString(R.string.auth_register_error_email_not_real)
                        )
                }
            }
        })
    }
}
