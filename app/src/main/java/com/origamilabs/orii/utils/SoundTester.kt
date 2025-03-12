package com.origamilabs.orii.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.getSystemService
import java.util.Locale

/**
 * Classe permettant de tester la lecture audio.
 * Les affichages utilisateurs et les commentaires sont en français, le reste est en anglais.
 */
class SoundTester(
    context: Context,
    private val resourceProvider: ResourceProvider,
    private val onCompletedCallback: () -> Unit
) {
    // Utilisation de l'extension KTX pour obtenir AudioManager de façon idiomatique
    private val audioManager: AudioManager = context.getSystemService()!!

    // Initialisation paresseuse du MediaPlayer
    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer.create(context, resourceProvider.eAudioDemoNonna).apply {
            setOnCompletionListener { onCompletedCallback() }
        }
    }

    // TextToSpeech sera initialisé de façon asynchrone
    private var tts: TextToSpeech? = null
    private val speakMessage: String = resourceProvider.ttsTestMessage

    // Détermine si l'on utilise TextToSpeech en fonction de la langue et du pays
    private val useTTS: Boolean = when {
        Locale.getDefault().language.equals("en", ignoreCase = true) -> false
        Locale.getDefault().language.equals("fr", ignoreCase = true) -> false
        Locale.getDefault().let { it.language.equals("zh", ignoreCase = true) && it.country.equals("TW", ignoreCase = true) } -> false
        Locale.getDefault().language.equals("ja", ignoreCase = true) -> false
        else -> true
    }

    init {
        // Optionnel : ajustement de la locale pour Hong Kong si nécessaire
        if (Locale.getDefault().language.equals("zh", ignoreCase = true) &&
            Locale.getDefault().country.equals("HK", ignoreCase = true)
        ) {
            // Intentionnellement vide - aucun ajustement requis pour HK
            Unit
        }
        if (useTTS) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.apply {
                        setTTSLanguage(this)
                        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) = Unit
                            @Deprecated("Deprecated in Java", ReplaceWith(""), level = DeprecationLevel.WARNING)
                            override fun onError(utteranceId: String?) = Unit
                            override fun onDone(utteranceId: String?) {
                                onCompletedCallback()
                            }
                        })
                    }
                }
            }
        }
    }

    private fun setTTSLanguage(tts: TextToSpeech) {
        val locale = if (Locale.getDefault().let {
                it.language.equals("zh", ignoreCase = true) && it.country.equals("HK", ignoreCase = true)
            }) Locale("yue", "HK")
        else Locale.getDefault()
        tts.language = locale
    }

    // Méthode privée pour jouer l'audio
    private fun playAudio() {
        if (useTTS) {
            tts?.apply {
                setTTSLanguage(this)
                speak(speakMessage, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            } ?: error("TextToSpeech is null")
        } else {
            mediaPlayer.apply {
                seekTo(0)
                start()
            }
        }
    }

    fun stopAudio() {
        if (useTTS) {
            tts?.stop()
        } else if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun toggleAudio(): Boolean {
        return if (useTTS) {
            tts?.let {
                if (it.isSpeaking) {
                    stopAudio()
                    false
                } else {
                    playAudio()
                    true
                }
            } ?: error("TextToSpeech is null")
        } else {
            if (mediaPlayer.isPlaying) {
                stopAudio()
                false
            } else {
                playAudio()
                true
            }
        }
    }

    fun close() {
        runCatching {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        tts?.apply {
            stop()
            shutdown()
        }
        tts = null
    }
}
