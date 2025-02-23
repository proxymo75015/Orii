package com.origamilabs.orii.notification.models

/**
 * Représente une conversation (chat) associée à un expéditeur.
 *
 * @param sender L'expéditeur du chat.
 * @param message Le premier message du chat.
 */
class Chat(var sender: String, message: String) {

    /**
     * Liste des messages de la conversation.
     * Le setter est privé afin d'éviter une modification non contrôlée depuis l'extérieur.
     */
    var messages: MutableList<String> = mutableListOf(message)
        private set

    /**
     * Ajoute un message au chat.
     *
     * @param message Le message à ajouter.
     */
    fun addMessage(message: String) {
        messages.add(message)
    }

    /**
     * Supprime le message situé à l'index spécifié.
     *
     * @param index L'index du message à supprimer.
     */
    fun removeMessage(index: Int) {
        messages.removeAt(index)
    }

    /**
     * Retourne le premier message du chat.
     *
     * @param remove Si vrai, le message est également supprimé du chat.
     * @return Le premier message.
     */
    fun getEarliestMessage(remove: Boolean = false): String {
        return if (remove) {
            messages.removeAt(0)
        } else {
            messages.first()
        }
    }
}
