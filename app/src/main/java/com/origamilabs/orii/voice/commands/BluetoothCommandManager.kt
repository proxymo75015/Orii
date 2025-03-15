package com.origamilabs.orii.voice.commands

import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothCommandManager @Inject constructor(private val commandManager: CommandManager) {

    /**
     * Notifie l'activation de l'assistant vocal via Bluetooth.
     * Envoie les codes spécifiques : 51 (state changed, activé) et 52 (voice assistant counter).
     */
    fun notifyVoiceAssistantActivated() {
        commandManager.sendCommand(51.toByte(), 1, byteArrayOf(1))
        commandManager.sendCommand(52.toByte(), 1, byteArrayOf(1))
    }

    /**
     * Notifie la désactivation de l'assistant vocal via Bluetooth.
     * Envoie le code spécifique : 51 (state changed, désactivé).
     */
    fun notifyVoiceAssistantDeactivated() {
        commandManager.sendCommand(51.toByte(), 1, byteArrayOf(0))
    }

    /**
     * Envoie un SMS via Bluetooth.
     *
     * @param text Texte du SMS à envoyer
     * @return true si l'envoi est réussi, false sinon
     */
    fun sendSms(text: String): Boolean {
        return commandManager.sendSms(text)
    }
}
