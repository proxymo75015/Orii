package com.origamilabs.orii.voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Activity VoiceAssistantActivity
 *
 * Gère l’interaction vocale (Text-to-Speech et Speech-to-Text) et notifie via CommandManager
 * l’activation (code 51 et 52) et la désactivation de l’assistant vocal.
 *
 * Le module VOICE_ASSISTANT intégré dans le projet se base sur ces commandes pour mettre à jour
 * l’état de l’assistant et le compteur d’activation.
 */
@AndroidEntryPoint
class VoiceAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    @Inject
    lateinit var commandManager: CommandManager

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

        // Initialisation du TTS
        tts = TextToSpeech(this, this)

        // Configuration du SpeechRecognizer
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

        // Enregistrement du receiver pour la désactivation via triple tap
        registerReceiver(assistantDisableReceiver, IntentFilter("com.origamilabs.orii.ACTION_DISABLE_ASSISTANT"))

        // Notifier le périphérique que l'assistant vocal est activé
        notifyVoiceAssistantActivated()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRANCE
            tts?.setSpeechRate(1.0f)
            speak("Assistant vocal activé. Dites une commande pour commencer.")
            // Vous pouvez lancer automatiquement la reconnaissance vocale si souhaité :
            // startSpeechRecognition()
        } else {
            Log.e("VoiceAssistant", "Erreur lors de l'initialisation du TTS")
        }
    }

    /**
     * Notifie l'activation de l'assistant au périphérique Bluetooth.
     *
     * Utilise le code 51 (state changed) avec payload 1 pour indiquer "activé"
     * et le code 52 (voice assistant counter) pour mettre à jour le compteur.
     */
    private fun notifyVoiceAssistantActivated() {
        // Envoi de la commande d'activation via l'instance injectée de CommandManager.
        commandManager.sendCommand(51.toByte(), 1, byteArrayOf(1))
        commandManager.sendCommand(52.toByte(), 1, byteArrayOf(1))
        Log.d("VoiceAssistant", "Notifié au périphérique: assistant activé (codes 51 et 52)")
    }

    /**
     * Notifie la désactivation de l'assistant au périphérique Bluetooth (code 51 avec payload 0).
     */
    private fun notifyVoiceAssistantDeactivated() {
        commandManager.sendCommand(51.toByte(), 1, byteArrayOf(0))
        Log.d("VoiceAssistant", "Notifié au périphérique: assistant désactivé")
    }

    /**
     * Lance la reconnaissance vocale.
     */
    fun startSpeechRecognition() {
        speechRecognizer.startListening(speechIntent)
    }

    /**
     * Traite la commande vocale reconnue.
     */
    private fun handleVoiceCommand(command: String) {
        Log.d("VoiceAssistant", "Commande vocale reconnue: $command")
        speak("Vous avez dit: $command")
// Appel à ChatGPT pour obtenir une réponse
