package com.origamilabs.orii.notification.listeners

/**
 * Interface pour écouter la réception de notifications.
 */
interface OnNotificationReceivedListener {
    /**
     * Appelée lors de la réception d'une notification.
     *
     * @param packageName Le nom du package de l'application à l'origine de la notification.
     * @param sender L'expéditeur de la notification.
     * @param message Le contenu du message de la notification.
     */
    fun onNotificationReceived(packageName: String, sender: String, message: String)
}
