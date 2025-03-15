package com.origamilabs.orii.voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.origamilabs.orii.R
import com.origamilabs.orii.network.ChatGPTApiClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class VoiceAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    private var isSpeaking = false
    private var wasOffline = false
    private val handler = Handler(Looper.getMainLooper())
    private var finalPauseRunnable: Runnable? = null
    private val accumulatedCommand = StringBuilder()

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isInternetAvailable()) {
                if (wasOffline && !isAssistantBusy()) {
                    speak("Connexion Internet rétablie. Votre assistant virtuel est de nouveau pleinement opérationnel.")
                    wasOffline = false
                }
            } else {
                if (!wasOffline) {
                    speak("Votre assistant virtuel n'a plus accès à Internet. Seules les commandes vocales hors ligne sont disponibles.")
                    wasOffline = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_assistant)

        tts = TextToSpeech(this, this)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(partialResults: Bundle?) {
                val partialCommand = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
                lifecycleScope.launch {
                    ChatGPTApiClient.sendIntermediateRequest(partialCommand)
                }
                accumulatedCommand.append(" ").append(partialCommand)
                resetFinalPauseTimer()
            }

            override fun onResults(results: Bundle?) {
                val finalCommand = accumulatedCommand.toString().trim()
                lifecycleScope.launch {
                    val chatResponse = ChatGPTApiClient.sendFinalRequest(finalCommand)
                    speak(chatResponse)
                }
                accumulatedCommand.clear()
            }

            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { speak("Erreur lors de la reconnaissance vocale") }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun resetFinalPauseTimer() {
        finalPauseRunnable?.let { handler.removeCallbacks(it) }
        finalPauseRunnable = Runnable {
            speechRecognizer.stopListening()
        }
        handler.postDelayed(finalPauseRunnable!!, 2000L)
    }

    override fun onDestroy() {
        unregisterReceiver(connectivityReceiver)
        tts.shutdown()
        speechRecognizer.destroy()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.FRANCE
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) { isSpeaking = true }
                override fun onDone(utteranceId: String) { isSpeaking = false }
                override fun onError(utteranceId: String) { isSpeaking = false }
            })
            speak("Je suis ton assistant virtuel. Comment puis-je t’aider ?")
        }
    }

    private fun speak(message: String) {
        if (!tts.isSpeaking) {
            isSpeaking = true
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        }
    }

    private fun isAssistantBusy(): Boolean = isSpeaking

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}