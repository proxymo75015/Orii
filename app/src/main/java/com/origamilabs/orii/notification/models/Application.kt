package com.origamilabs.orii.notification.models

/**
 * Classe gérant les chats au sein de l'application de notifications.
 *
 * Elle permet d'ajouter des messages à des chats existants ou de créer
 * de nouveaux chats si aucun chat n'existe pour un expéditeur donné.
 */
class Application {

    // Liste interne de chats, initialisée dès la déclaration.
    private val _chats = arrayListOf<Chat>()

    /**
     * Retourne la liste des chats.
     */
    val chats: ArrayList<Chat>
        get() = _chats

    /**
     * Retourne le premier chat (le plus ancien).
     *
     * @throws NoSuchElementException si la liste des chats est vide.
     */
    val earliestChat: Chat
        get() = _chats.first()

    /**
     * Ajoute un message à un chat existant ou crée un nouveau chat si nécessaire.
     *
     * @param sender L'expéditeur du message.
     * @param message Le contenu du message.
     */
    fun addMessage(sender: String, message: String) {
        // Recherche d'un chat existant pour le même expéditeur
        val existingChat = _chats.find { it.sender == sender }
        if (existingChat != null) {
            existingChat.addMessage(message)
        } else {
            _chats.add(Chat(sender, message))
        }
    }

    /**
     * Supprime le chat situé à l'index spécifié.
     *
     * @param index L'index du chat à supprimer.
     */
    fun removeChat(index: Int) {
        _chats.removeAt(index)
    }

    /**
     * Supprime le chat spécifié.
     *
     * @param chat Le chat à supprimer.
     */
    fun removeChat(chat: Chat) {
        _chats.remove(chat)
    }
}
