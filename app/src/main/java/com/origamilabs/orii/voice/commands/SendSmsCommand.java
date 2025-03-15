package com.origamilabs.orii.voice.commands

import com.origamilabs.orii.core.bluetooth.manager.CommandManager

object SendSmsCommand {
fun execute(command: String, commandManager: CommandManager, speak: (String) -> Unit) {
val smsText = command.removePrefix("envoyer un sms").trim()
        if (smsText.isNotEmpty() && commandManager.sendSms(smsText)) {
speak("SMS envoyé : $smsText")
        } else {
speak("Erreur lors de l'envoi du SMS ou texte manquant")
        }
                }
                }
