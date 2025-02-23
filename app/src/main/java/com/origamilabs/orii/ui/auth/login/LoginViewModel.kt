package com.origamilabs.orii.ui.auth.login

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.origamilabs.orii.R
import com.origamilabs.orii.api.API
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.User
import com.origamilabs.orii.utils.Validator

class LoginViewModel : ViewModel() {

    companion object {
        const val TAG = "LoginViewModel"
    }

    // La propriété fragment doit être initialisée par le LoginFragment
    lateinit var fragment: LoginFragment

    fun setFragment(fragment: LoginFragment) {
        this.fragment = fragment
    }

    fun handleLogin(email: String, password: String) {
        // Réinitialisation des erreurs
        fragment.setEmailError(null)
        fragment.setPasswordError(null)

        // Vérification de la validité de l'email
        if (email.isEmpty()) {
            fragment.setEmailError(fragment.getString(R.string.auth_error_email_is_empty))
            return
        }
        if (!Validator.isEmailValid(email)) {
            fragment.setEmailError(fragment.getString(R.string.auth_error_invalid_email_format))
            return
