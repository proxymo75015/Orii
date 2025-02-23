package com.origamilabs.orii.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.origamilabs.orii.R
import java.util.Locale

class SoundTester(
    context: Context,
    private val onCompletedCallback: () -> Unit
) {
    private val TAG = "SoundTester"
    private val audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var speakMessage: String
    private val useTTS: Boolean

    init {
        // Création du MediaPlayer pour lire un fichier audio local.
        mediaPlayer = MediaPlayer.create(context, R.raw.e_audio_demo_nonna).apply {
            setOnCompletionListener {
                onCompletedCallback()
            }
        }

        // Récupération du service audio.
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: throw TypeCastException("null cannot be cast to non-null type android.media.AudioManager")

        // Détermination de la locale et du mode de lecture (TTS ou audio) en fonction de la langue.
        var locale = Locale.getDefault()
        useTTS = when {
            locale.language.equals("en", ignoreCase = true) -> false
            locale.language.equals("zh", ignoreCase = true) && locale.country.equals("TW", ignoreCase = true) -> false
            locale.language.equals("ja", ignoreCase = true) -> false
            else -> true
        }

        // Cas particulier pour Hong Kong : conversion en cantonais.
        if (locale.language.equals("zh", ignoreCase = true) && locale.country.equals("HK", ignoreCase = true)) {
            locale = Locale("yue", "HK")
        }

        Log.d(TAG, locale.displayCountry)
        Log.d(TAG, locale.displayLanguage)
        Log.d(TAG, locale.language)

        // Initialisation du TextToSpeech si nécessaire.
        if (useTTS && tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let {
                        setTTSLanguage(it)
                        it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}
                            override fun onError(utteranceId: String?) {}
                            override fun onDone(utteranceId: String?) {
                                onCompletedCallback()
                            }
                        })
                    }
                }
            }
        }

        // Message à lire par le TTS.
        speakMessage = context.getString(R.string.tts_test_message)
    }

    /**
     * Configure la langue du TextToSpeech en fonction de la locale courante.
     */
    fun setTTSLanguage(tts: TextToSpeech) {
        var locale = Locale.getDefault()
        if (locale.language.equals("zh", ignoreCase = true) && locale.country.equals("HK", ignoreCase = true)) {
            locale = Locale("yue", "HK")
        }
        Log.d(TAG, "Selected locale: $locale")
        Log.d(TAG, "Selected language: ${locale.language}")
        tts.language = locale
    }

    /**
     * Lance la lecture audio. Si le TTS est utilisé, le message textuel est lu ; sinon, le MediaPlayer est démarré.
     */
    fun playAudio() {
        if (useTTS) {
            tts?.let {
                setTTSLanguage(it)
                it.speak(speakMessage, TextToSpeech.QUEUE_FLUSH, null, null)
            } ?: throw NullPointerException("TextToSpeech is null")
        } else {
            mediaPlayer?.let {
                it.seekTo(0)
                it.start()
            } ?: throw NullPointerException("MediaPlayer is null")
        }
    }

    /**
     * Arrête la lecture audio. Pour le TTS, la lecture en cours est arrêtée ; pour le MediaPlayer,
     * celui-ci est mis en pause s'il est en train de jouer.
     */
    fun stopAudio() {
        if (useTTS) {
            tts?.stop()
        } else {
            mediaPlayer?.let {
                if (it.isPlaying) it.pause()
            }
        }
    }

    /**
     * Bascule l'état de lecture audio.
     *
     * @return `true` si la lecture a été démarrée, `false` si elle a été arrêtée.
     */
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
            } ?: throw NullPointerException("TextToSpeech is null")
        } else {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    stopAudio()
                    false
                } else {
                    playAudio()
                    true
                }
            } ?: throw NullPointerException("MediaPlayer is null")
        }
    }

    /**
     * Libère les ressources utilisées par le MediaPlayer et le TextToSpeech.
     */
    fun close() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        tts?.apply {
            stop()
            shutdown()
        }
        tts = null
    }
}
