package com.origamilabs.orii.handler

import android.content.Context
import com.origamilabs.orii.R
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.Application
import com.origamilabs.orii.notification.models.MessageCollection

class MessageHandler {

    private val messages = MessageCollection()

    fun getSpeechQueue(context: Context): ArrayList<String> {
        val speechQueue = arrayListOf<String>()
        // Tant qu'il y a des messages dans la collection…
        while (messages.size() > 0) {
            // Récupère la liste des expéditeurs
            val senders = messages.senders
            var earliestSenderIndex = 0
            // Recherche de l'expéditeur avec le timestamp le plus ancien
            for ((index, sender) in senders.withIndex()) {
                if (sender.timestamp < senders[0].timestamp) {
                    earliestSenderIndex = index
                    break
                }
            }
            val sender = senders[earliestSenderIndex]
            val timeSlots = sender.timeSlots
            // On prend la première tranche horaire
            val timeSlot = timeSlots[0]
            // Ajoute le message indiquant que [nom] a dit...
            speechQueue.add("${sender.name} ${context.getString(R.string.tts_message_said)}")
            // Ajoute chaque message de la tranche horaire et log l'action
            for (msg in timeSlot.messages) {
                speechQueue.add(msg.messageContent)
                AnalyticsManager.INSTANCE.logReadout(msg.appName, msg.messageContent.length)
            }
            // Ajoute le message de fin
            speechQueue.add(context.getString(R.string.tts_message_ending))
            // Retire la tranche horaire traitée
            sender.removeTimeSlot()
            // Si l'expéditeur n'a plus de tranches horaires, le retirer de la collection
            if (timeSlots.isEmpty()) {
                senders.remove(sender)
            }
        }
        // Vide la collection après traitement
        messages.clear()
        return speechQueue
    }

    fun addMessage(packageName: String, sender: String, message: String) {
        for (app in AppManager.INSTANCE.availableApps) {
            if (app.packageName == packageName && app.ledColor != 0 && app.vibration != 0) {
                messages.add(app.appName, sender, message, (System.currentTimeMillis() / 1000).toInt())
                return
            }
        }
    }
}
