package com.origamilabs.orii.notification.listeners

/**
 * Interface définissant le listener pour la réception de SMS.
 */
interface OnSmsReceivedListener {
    /**
     * Appelé lors de la réception d'un SMS.
     *
     * @param sender L'expéditeur du SMS.
     * @param message Le contenu du SMS.
     */
    fun onSmsReceived(sender: String, message: String)
}
