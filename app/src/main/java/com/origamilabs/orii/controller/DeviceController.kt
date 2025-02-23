package com.origamilabs.orii.controller

import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.telecom.TelecomManager
import android.view.KeyEvent
import android.widget.Toast

/**
 * Contrôleur de périphériques permettant d'exécuter diverses actions sur l'appareil.
 *
 * Il fournit des méthodes pour :
 * - Basculer le mode "Ne pas déranger" (notification interruption filter)
 * - Activer/désactiver la lampe torche (flashlight)
 * - Verrouiller l'écran (ou réveiller l'appareil si l'écran est éteint)
 * - Simuler la lecture/pause des médias
 * - Appeler l'assistant vocal
 * - Répondre à un appel entrant
 * - Envoyer un événement de touche média
 *
 * @property context Le contexte de l'application (non‑nullable).
 */
class DeviceController(private val context: Context) {

    private var flashLightStatus: Boolean = false

    fun getContext(): Context = context

    fun switchDisturbMode() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: throw TypeCastException("NotificationManager non disponible")
        // Si le filtre d'interruption actuel est PRIORITY (1), on le passe à NONE (3) ; sinon, on le passe à PRIORITY (1)
        notificationManager.interruptionFilter =
            if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                NotificationManager.INTERRUPTION_FILTER_NONE
            else
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
    }

    fun switchFlashlight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Utilisation de l'API Camera2 pour activer/désactiver la lampe torche
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: throw TypeCastException("CameraManager non disponible")
            val cameraId = cameraManager.cameraIdList.first()
            cameraManager.setTorchMode(cameraId, !flashLightStatus)
            flashLightStatus = !flashLightStatus
        } else {
            // Utilisation de l'API Camera dépréciée pour les anciennes versions d'Android
            val camera = Camera.open()
            val parameters = camera.parameters
            when (parameters.flashMode) {
                "FLASH_MODE_TORCH" -> {
                    val newParams = camera.parameters
                    // Ici, nous utilisons "off" pour désactiver la lampe torche
                    newParams.flashMode = "off"
                    camera.parameters = newParams
                    camera.startPreview()
                }
                "FLASH_MODE_OFF" -> {
                    parameters.flashMode = "torch"
                    camera.parameters = parameters
                    camera.startPreview()
                }
            }
        }
    }

    fun switchScreenLock() {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: throw TypeCastException("DevicePolicyManager non disponible")
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: throw TypeCastException("PowerManager non disponible")
        // Si l'écran est allumé (isInteractive), on verrouille l'appareil.
        if (powerManager.isInteractive) {
            devicePolicyManager.lockNow()
        } else {
            // Sinon, on crée et libère un wake lock pour réveiller l'appareil brièvement.
            val wakeLock = powerManager.newWakeLock(
                // Ces flags sont dépréciés, mais utilisés ici pour conserver le comportement original.
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "DeviceController:WAKE_LOCK"
            )
            wakeLock.acquire()
            wakeLock.release()
        }
    }

    fun callMediaPlayOrPause() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun callCameraShutter() {
        Toast.makeText(
            context,
            "We need to call HID devices to trigger camera shutter",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun callVoiceAssistant() {
        val intent = Intent("android.intent.action.VOICE_COMMAND").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun pickUpCall() {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            ?: throw TypeCastException("TelecomManager non disponible")
        if (context.checkSelfPermission("android.permission.ANSWER_PHONE_CALLS") == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            telecomManager.acceptRingingCall()
        }
    }

    private fun sendMediaKeyEvent(keyEvent: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: throw TypeCastException("AudioManager non disponible")
        // Envoie les événements KEY_DOWN et KEY_UP pour le code de touche spécifié
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyEvent))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyEvent))
    }
}
