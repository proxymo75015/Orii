package com.origamilabs.orii.notification.processor.line

import android.util.Log
import com.origamilabs.orii.notification.processor.Processor

/**
 * Processor pour les notifications provenant de l'application LINE.
 *
 * Utilise le titre comme expéditeur et le texte de la notification comme message.
 * La méthode [isValidMessage] permet d'ignorer les messages dupliqués.
 */
class LineProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : Processor(tickerText, title, text, textLines) {

    companion object {
        private var previousMessage: String? = null
        private const val TAG = "LineProcessor"
    }

    override fun isGroupChat(): Boolean = false

    override fun getSender(): String = title

    override fun getMessage(): String = text.orEmpty()

    /**
     * Vérifie si le message actuel est valide.
     *
     * Si le message précédent est identique au message actuel, il est considéré comme dupliqué,
     * la variable [previousMessage] est réinitialisée et la méthode retourne false.
     * Sinon, le message actuel est mémorisé et la méthode retourne true.
     *
     * @return true si le message est valide, false sinon.
     */
    fun isValidMessage(): Boolean {
        Log.d(TAG, "Previous message: $previousMessage")
        if (previousMessage != null && previousMessage == text) {
            previousMessage = null
            return false
        }
        previousMessage = text
        return true
    }
}
