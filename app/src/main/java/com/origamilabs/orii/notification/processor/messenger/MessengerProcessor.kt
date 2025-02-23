package com.origamilabs.orii.notification.processor.messenger

import com.origamilabs.orii.notification.processor.Processor

/**
 * Processor pour les notifications de Messenger.
 *
 * Utilise le titre comme expéditeur et extrait le contenu du message.
 * Si le texte commence par le titre suivi d'un caractère spécial (：) et d'un caractère supplémentaire,
 * ce préfixe est supprimé.
 */
class MessengerProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : Processor(tickerText, title, text, textLines) {

    companion object {
        private const val TAG = "MessengerProcessor"
    }

    override fun isGroupChat(): Boolean = false

    override fun getSender(): String = title

    override fun getMessage(): String {
        val currentText = text ?: throw NullPointerException("text is null")
        return if (currentText.length > title.length) {
            // Extraire le préfixe de la longueur du titre
            val prefix = currentText.substring(0, title.length)
            // Vérifier si le préfixe correspond au titre suivi du caractère spécial « ： »
            if (prefix == title + '：') {
                // Supprime le préfixe (titre + 2 caractères) et retourne le reste du message
                currentText.substring(title.length + 2)
            } else {
                currentText
            }
        } else {
            currentText
        }
    }
}
