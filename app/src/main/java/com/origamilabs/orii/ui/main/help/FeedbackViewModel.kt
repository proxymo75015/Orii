package com.origamilabs.orii.ui.main.help

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.origamilabs.orii.R
import com.origamilabs.orii.api.API
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.User
import com.origamilabs.orii.ui.auth.AuthActivity

/**
 * ViewModel qui gère la soumission du feedback.
 *
 * La méthode [handleFeedbackSubmit] vérifie d'abord que le texte n'est pas vide.
 * Si c'est le cas, elle affiche une erreur sur l'interface.
 * Sinon, elle récupère l'utilisateur courant et effectue un appel API pour poster le feedback.
 * En cas de succès, la vue affiche une boîte de dialogue de succès.
 * En cas d'erreur (notamment si le token a expiré), l'utilisateur est redirigé vers l'écran d'authentification.
 */
class FeedbackViewModel : ViewModel() {

    fun handleFeedbackSubmit(activity: FeedbackActivity, text: String) {
        if (text.isEmpty()) {
            val errorMsg = activity.getString(R.string.feedback_input_error)
            activity.setFeedbackError(errorMsg)
        } else {
            val currentUser: User = AppManager.currentUser
                ?: throw NullPointerException("Current user is null")
            API.postFeedback(
                currentUser.token,
                currentUser.id,
                text,
                AppManager.uuid,
                "2.2.16",
                object : API.ResponseListener {
                    override fun onSuccess(response: JsonObject) {
                        activity.showSuccessDialog()
                    }

                    override fun onError(errorMessage: String) {
                        if (errorMessage == "token_expired_message") {
                            val intent = Intent(activity, AuthActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            activity.startActivity(intent)
                        }
                    }
                }
            )
        }
    }
}
