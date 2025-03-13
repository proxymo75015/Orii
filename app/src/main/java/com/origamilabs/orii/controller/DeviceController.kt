package com.origamilabs.orii.controller

import android.Manifest
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.telecom.TelecomManager
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.origamilabs.orii.ui.WakeUpActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceController @Inject constructor(@ApplicationContext private val context: Context) {

    fun getContext(): Context = context

    /**
     * Bascule le mode "Ne pas déranger" entre PRIORITY et NONE.
     * Pour API < 23, la fonctionnalité n'est pas supportée.
     */
    fun switchDisturbMode() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: throw IllegalStateException("NotificationManager non disponible")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(
                when (notificationManager.currentInterruptionFilter) {
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY -> NotificationManager.INTERRUPTION_FILTER_NONE
                    else -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                }
            )
        } else {
            Toast.makeText(
                context,
                "Mode Ne pas déranger indisponible sur cette version d'Android",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Verrouille l'écran si l'appareil est actif, ou réveille l'appareil sinon en lançant une Activity dédiée.
     */
    fun switchScreenLock() {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: throw IllegalStateException("DevicePolicyManager non disponible")
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: throw IllegalStateException("PowerManager non disponible")

        if (powerManager.isInteractive) {
            devicePolicyManager.lockNow()
        } else {
            // Lance l'Activity WakeUpActivity pour réveiller l'appareil
            val wakeIntent = Intent(context, WakeUpActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(wakeIntent)
        }
    }

    /**
     * Envoie un événement média pour jouer ou mettre en pause.
     */
    fun callMediaPlayOrPause() {
        sendMediaKeyEvent()
    }

    /**
     * Lance l'assistant vocal via l'intent approprié.
     */
    fun callVoiceAssistant() {
        val intent = Intent("android.intent.action.VOICE_COMMAND").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Tente de décrocher un appel entrant (API >= 26).
     * Vérifie la permission ANSWER_PHONE_CALLS avant d'appeler acceptRingingCall().
     */
    fun pickUpCall() {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            ?: throw IllegalStateException("TelecomManager non disponible")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                @Suppress("deprecation")
                telecomManager.acceptRingingCall()
            } else {
                Toast.makeText(
                    context,
                    "Permission ANSWER_PHONE_CALLS non accordée",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Fonction décrocher indisponible sur cette version d'Android",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Envoie un événement média (KEY_DOWN puis KEY_UP) via l'AudioManager.
     * La valeur KEYCODE_MEDIA_PLAY_PAUSE est utilisée en dur.
     */
    private fun sendMediaKeyEvent() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: throw IllegalStateException("AudioManager non disponible")
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
    }
}
