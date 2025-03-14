package com.origamilabs.orii.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Ce BroadcastReceiver intercepte les intents générés par CommandManager pour les gestes de type tap.
 * - ACTION_GESTURE_REVERSE_DOUBLE_TAP (double tap) : lance l'assistant vocal.
 * - ACTION_GESTURE_FLAT_TRIPLE_TAP (triple tap) : envoie un broadcast pour désactiver l'assistant.
 */
class GestureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.origamilabs.orii.ACTION_GESTURE_REVERSE_DOUBLE_TAP" -> {
                Timber.d("Double tap détecté, lancement de l'assistant vocal")
                val activityIntent = Intent(context, Class.forName("com.origamilabs.orii.voice.VoiceAssistantActivity"))
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(activityIntent)
            }
            "com.origamilabs.orii.ACTION_GESTURE_FLAT_TRIPLE_TAP" -> {
                Timber.d("Triple tap détecté, désactivation de l'assistant vocal")
                val disableIntent = Intent("com.origamilabs.orii.ACTION_DISABLE_ASSISTANT")
                context.sendBroadcast(disableIntent)
            }
            else -> Timber.d("Geste non géré: ${intent.action}")
        }
    }
}
