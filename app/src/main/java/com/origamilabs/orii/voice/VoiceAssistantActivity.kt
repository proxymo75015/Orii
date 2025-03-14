package com.origamilabs.orii.voice

import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.connection.HeadsetHandler
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class VoiceAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    @Inject
    lateinit var commandManager: CommandManager

    @Inject
    lateinit var headsetHandler: HeadsetHandler

    private var tts: TextToSpeech? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    // Receiver pour désactiver l'assistant via triple tap (ACTION_GESTURE_FLAT_TRIPLE_TAP)
    private val assistantDisableReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.origamilabs.orii.ACTION_DISABLE_ASSISTANT") {
                speak("Assistant vocal désactivé.")
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_assistant)

        // Initialisation du Text-to-Speech (TTS)
        tts = TextToSpeech(this, this)

        // Initialisation du SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceAssistant", "Prêt pour la parole")
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceAssistant", "Début de la parole")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("VoiceAssistant", "Fin de la parole")
            }

            override fun onError(error: Int) {
                Log.e("VoiceAssistant", "Erreur de reconnaissance vocale: $error")
                speak("Erreur lors de la reconnaissance vocale")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = matches?.firstOrNull() ?: ""
                handleVoiceCommand(command)
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // Configuration de l'intention pour la reconnaissance vocale
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
        }

        // Enregistrement du receiver pour désactiver l'assistant via triple tap
        registerReceiver(assistantDisableReceiver, IntentFilter("com.origamilabs.orii.ACTION_DISABLE_ASSISTANT"))

        // Notifier le périphérique Bluetooth que l'assistant vocal est activé
        notifyVoiceAssistantActivated()

        // Vérifier la connexion au casque Bluetooth via HeadsetHandler
        if (headsetHandler.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            Log.d("VoiceAssistant", "Casque Bluetooth connecté, audio routé vers le headset.")
        } else {
            Log.d("VoiceAssistant", "Aucun casque Bluetooth détecté. Vérifiez la connexion.")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRANCE
            tts?.setSpeechRate(1.0f)
            speak("Assistant vocal activé. Dites une commande pour commencer.")
            // Vous pouvez démarrer automatiquement la reconnaissance vocale si souhaité :
            // startSpeechRecognition()
        } else {
            Log.e("VoiceAssistant", "Erreur lors de l'initialisation du TTS")
        }
    }

    /**
     * Notifie l'activation de l'assistant au périphérique Bluetooth.
     * Utilise le code 51 (state changed) avec payload 1 pour indiquer "activé"
     * et le code 52 (voice assistant counter) pour mettre à jour le compteur.
     */
    private fun notifyVoiceAssistantActivated() {
        commandManager.sendCommand(51.toByte(), 1, byteArrayOf(1))
        commandManager.sendCommand(52.toByte(), 1, byteArrayOf(1))
        Log.d("VoiceAssistant", "Assistant activé (codes 51 et 52 envoyés)")
    }

    /**
     * Notifie la désactivation de l'assistant au périphérique Bluetooth (code 51 avec payload 0).
     */
    private fun notifyVoiceAssistantDeactivated() {
        commandManager.sendCommand(51.toByte(), 1, byteArrayOf(0))
        Log.d("VoiceAssistant", "Assistant désactivé (code 51 envoyé)")
    }

    /**
     * Démarre la reconnaissance vocale.
     */
    fun startSpeechRecognition() {
        speechRecognizer.startListening(speechIntent)
    }

    /**
     * Vérifie la disponibilité d'une connexion Internet.
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Traite la commande vocale reconnue.
     *
     * Si la connexion Internet est disponible, la commande est gérée par ChatGPT.
     * Sinon, la commande est traitée localement (mode autonome).
     *
     * La commande "envoyer sms" est également traitée pour envoyer un SMS via le périphérique.
     */
    private fun handleVoiceCommand(command: String) {
        Log.d("VoiceAssistant", "Commande vocale reconnue: $command")

        // Si la commande commence par "envoyer sms", on la traite pour envoyer un SMS
        if (command.startsWith("envoyer sms", ignoreCase = true)) {
            val smsText = command.removePrefix("envoyer sms").trim()
            if (smsText.isNotEmpty()) {
                if (commandManager.sendSms(smsText)) {
                    speak("SMS envoyé : $smsText")
                } else {
                    speak("Erreur lors de l'envoi du SMS")
                }
            } else {
                speak("Aucun texte trouvé pour le SMS")
            }
        } else {
            // Si Internet est disponible, on laisse ChatGPT gérer la commande
            if (isInternetAvailable()) {
                speak("Vous avez dit: $command")
                lifecycleScope.launch {
                    val response = com.origamilabs.orii.chat.ChatGPTApiClient.sendRequest(command)
                    speak(response)
                }
            } else {
                // Si la connexion Internet est indisponible, on passe en mode autonome
                speak("Connexion Internet indisponible. Mode autonome activé. Traitement local de la commande: $command")
                // Traitement local de quelques commandes courantes
                when {
                    command.contains("heure", ignoreCase = true) -> {
                        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        speak("Il est $currentTime")
                    }
                    command.contains("allumer lampe", ignoreCase = true) -> {
                        // Vous pouvez intégrer ici l'appel à une méthode de DeviceController pour allumer une lampe
                        speak("Lampe allumée localement")
                    }
                    else -> {
                        speak("Commande traitée en mode autonome : $command")
                    }
                }
            }
        }
    }

    /**
     * Lit à voix haute le message.
     */
    fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        notifyVoiceAssistantDeactivated()
        unregisterReceiver(assistantDisableReceiver)
        tts?.stop()
        tts?.shutdown()
        speechRecognizer.destroy()
        super.onDestroy()
    }

    /**
     * Méthode associée à un bouton pour démarrer la reconnaissance vocale.
     */
    fun onStartListeningClicked(view: View) {
        startSpeechRecognition()
    }
}
