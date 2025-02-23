package com.origamilabs.orii.ui.auth

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.JsonObject
import com.origamilabs.orii.API
import com.origamilabs.orii.Constants
import com.origamilabs.orii.analytics.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.User
import java.util.*

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "AuthViewModel"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var fragment: AuthFragment
    private lateinit var googleSignClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    /**
     * Initialise le ViewModel avec le fragment d'authentification.
     */
    fun init(fragment: AuthFragment) {
        this.fragment = fragment
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.GOOGLE_ID_TOKEN_PRO)
            .requestEmail()
            .requestProfile()
            .build()
        val activity = fragment.activity ?: throw NullPointerException("Fragment activity is null")
        googleSignClient = GoogleSignIn.getClient(activity, gso)
        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        // Enregistrer un callback pour le login Facebook
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                // Gérer la réussite de la connexion Facebook (à personnaliser)
                Log.d(TAG, "Facebook login success")
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook login canceled")
            }

            override fun onError(error: FacebookException?) {
                Log.e(TAG, "Facebook login error", error)
            }
        })
    }

    /**
     * Lance le processus de login via Facebook.
     */
    fun handleFbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(fragment, listOf("email", "public_profile"))
    }

    /**
     * Lance le processus de login via Google.
     */
    fun handleGoogleLogin() {
        val signInIntent = googleSignClient.signInIntent
        fragment.startActivityForResult(signInIntent, 3)
    }

    /**
     * Authentifie l'utilisateur via Google en utilisant Firebase.
     */
    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${acct.id}")
        val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(fragment.activity as FragmentActivity, OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val currentUser: FirebaseUser = auth.currentUser
                        ?: throw NullPointerException("auth.currentUser is null")
                    Log.d(TAG, "logged user: ${currentUser.email}, ${acct.id}, ${acct.idToken}")
                    API.googleLogin(
                        currentUser.email!!,
                        acct.id!!,
                        acct.idToken!!,
                        AppManager.instance.uuid,
                        object : API.ResponseListener {
                            override fun onSuccess(response: JsonObject) {
                                val info = response.getAsJsonObject("user_info")
                                val id = info.get("users_id").asString
                                val email = info.get("users_email").asString
                                val name = info.get("users_name").asString
                                val token = info.get("users_token").asString
                                AppManager.instance.onUserLoggedIn(User(id, email, name, token))
                                AnalyticsManager.instance.logUserLogin(AnalyticsManager.LoginWay.GOOGLE)
                                fragment.navigateToTutorial()
                            }

                            override fun onError(errorMessage: String) {
                                fragment.setGoogleLoginError()
                            }
                        }
                    )
                } else {
                    val exceptionStr = task.exception?.toString() ?: ""
                    if (exceptionStr.contains("a network error", ignoreCase = true)) {
                        Toast.makeText(getApplication(), "Login fail, please check your network", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                    }
                }
            })
    }

    /**
     * Délègue le résultat de l'activité Facebook au CallbackManager.
     */
    fun setFbLoginActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Gère la connexion avec vérification à partir d'un userId et d'un verifyCode.
     */
    fun handleLoginWithVerify(userId: String, verifyCode: String) {
        API.loginWithVerify(userId, verifyCode, AppManager.instance.uuid, object : API.ResponseListener {
            override fun onSuccess(response: JsonObject) {
                val info = response.getAsJsonObject("user_info")
                val id = info.get("users_id").asString
                val email = info.get("users_email").asString
                val token = info.get("users_token").asString
                AppManager.instance.onUserLoggedIn(User(id, email, "", token))
                fragment.navigateToTutorial()
            }

            override fun onError(errorMessage: String) {
                if (errorMessage == "invalid_verify_message") {
                    fragment.setGoogleLoginError()
                }
            }
        })
    }
}
