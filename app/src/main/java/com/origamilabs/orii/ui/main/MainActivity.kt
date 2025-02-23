package com.origamilabs.orii.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.MainActivityBinding
import com.origamilabs.orii.services.OriiFirebaseMessagingService
import com.origamilabs.orii.ui.main.alerts.AlertsFragment
import com.origamilabs.orii.ui.main.help.HelpFragment
import com.origamilabs.orii.ui.main.home.HomeFragment
import com.origamilabs.orii.ui.main.home.update.reminder.ReminderFragment

/**
 * Activité principale de l'application.
 *
 * Gère la navigation par onglets, l'authentification (Google et Facebook) et
 * l'initialisation de Firebase Cloud Messaging.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SELECTED_TAB_INDEX = "selected_tab_index"
    }

    // Binding avec le layout via DataBinding.
    private lateinit var binding: MainActivityBinding
    private val viewModel: SharedViewModel by viewModels()

    // Pour conserver l'index de l'onglet courant.
    private var currentTabIndex: Int = 0

    // Google Sign-In.
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    // Facebook Login.
    private lateinit var callbackManager: CallbackManager

    // Utilisation de l'API moderne ActivityResult pour Google Sign-In.
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        handleSignInResult(task)
    }

    // Listener pour la sélection d'onglets.
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab) {}
        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabSelected(tab: TabLayout.Tab) {
            val tag = tab.tag?.toString() ?: ""
            Log.d(TAG, "Onglet sélectionné : $tag")
            val newFragment = when (tag) {
                "alerts" -> AlertsFragment.newInstance()
                "help" -> HelpFragment.newInstance()
                "home" -> HomeFragment.newInstance()
                "settings" -> ReminderFragment.newInstance() // Utilisation de ReminderFragment pour "settings"
                else -> HomeFragment.newInstance()
            }
            currentTabIndex = tab.position
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, newFragment, tag)
                .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération de l'onglet sélectionné avant rotation.
        currentTabIndex = savedInstanceState?.getInt(SELECTED_TAB_INDEX, 0) ?: 0

        // Initialisation du binding.
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Activation des vecteurs compatibles.
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Initialisations diverses.
        initTabs()
        initFirebaseMessaging()
        initGoogleSignIn()
        initFacebookLogin()
    }

    /**
     * Initialise la barre d'onglets.
     */
    private fun initTabs() {
        with(binding.tabs) {
            removeAllTabs()

            val tabHome = newTab().setText(R.string.tab_home).setTag("home")
            val tabAlerts = newTab().setText(R.string.tab_alerts).setTag("alerts")
            val tabSettings = newTab().setText(R.string.tab_settings).setTag("settings")
            val tabHelp = newTab().setText(R.string.tab_help).setTag("help")

            addTab(tabHome)
            addTab(tabAlerts)
            addTab(tabSettings)
            addTab(tabHelp)

            addOnTabSelectedListener(onTabSelectedListener)
            getTabAt(currentTabIndex)?.select()
        }
    }

    /**
     * Initialise Firebase Cloud Messaging pour les notifications push.
     */
    private fun initFirebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Erreur lors de la récupération du token FCM", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "Token FCM: $token")
        }
        OriiFirebaseMessagingService.fetchFirebaseToken()
    }

    /**
     * Configure Google Sign-In.
     */
    private fun initGoogleSignIn() {
        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_app_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Configure Facebook Login.
     */
    private fun initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }
                override fun onCancel() {
                    Log.d(TAG, "Connexion Facebook annulée.")
                }
                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Erreur lors de la connexion Facebook : ${error.message}")
                }
            })
    }

    /**
     * Lance l'authentification avec Google.
     */
    fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    /**
     * Lance l'authentification avec Facebook.
     */
    fun signInFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }

    // Pour la compatibilité avec Facebook.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Gère le résultat du Google Sign-In.
     */
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w(TAG, "Erreur Google Sign-In: ${e.statusCode}")
        }
    }

    /**
     * Authentifie l'utilisateur via Firebase avec les identifiants Google.
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Connexion réussie avec Google")
            } else {
                Log.w(TAG, "Erreur d'authentification Google", task.exception)
            }
        }
    }

    /**
     * Authentifie l'utilisateur via Firebase avec les identifiants Facebook.
     */
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Connexion réussie avec Facebook")
            } else {
                Log.w(TAG, "Erreur d'authentification Facebook", task.exception)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_INDEX, currentTabIndex)
    }
}
