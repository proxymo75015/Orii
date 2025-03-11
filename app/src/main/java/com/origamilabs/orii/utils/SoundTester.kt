package com.origamilabs.orii.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.origamilabs.orii.R
import java.util.Locale

/**
 * Classe permettant de tester la lecture audio.
 * Les affichages utilisateurs et les commentaires sont en français, le reste est en anglais.
 */
class SoundTester(
    context: Context,
    private val onCompletedCallback: () -> Unit
) {
    private val TAG = "SoundTester"
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.e_audio_demo_nonna).apply {
        setOnCompletionListener { onCompletedCallback() }
    }
    private var tts: TextToSpeech? = null
    private val speakMessage: String = context.getString(R.string.tts_test_message)
    private val useTTS: Boolean

    init {
        val locale = Locale.getDefault()
        useTTS = when {
            locale.language.equals("en", ignoreCase = true) -> false
            locale.language.equals("zh", ignoreCase = true) && locale.country.equals("TW", ignoreCase = true) -> false
            locale.language.equals("ja", ignoreCase = true) -> false
            else -> true
        }

        if (locale.language.equals("zh", ignoreCase = true) && locale.country.equals("HK", ignoreCase = true)) {
            // Ajustement de la locale pour Hong Kong si nécessaire
        }

        if (useTTS && tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.apply {
                        setTTSLanguage(this)
                        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
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
    }

    fun setTTSLanguage(tts: TextToSpeech) {
        var locale = Locale.getDefault()
        if (locale.language.equals("zh", ignoreCase = true) && locale.country.equals("HK", ignoreCase = true)) {
            locale = Locale("yue", "HK")
        }
        tts.language = locale
    }

    fun playAudio() {
        if (useTTS) {
            tts?.let {
                setTTSLanguage(it)
                it.speak(speakMessage, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            } ?: throw NullPointerException("TextToSpeech is null")
        } else {
            mediaPlayer?.apply {
                seekTo(0)
                start()
            } ?: throw NullPointerException("MediaPlayer is null")
        }
    }

    fun stopAudio() {
        if (useTTS) {
            tts?.stop()
        } else {
            mediaPlayer?.takeIf { it.isPlaying }?.pause()
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
