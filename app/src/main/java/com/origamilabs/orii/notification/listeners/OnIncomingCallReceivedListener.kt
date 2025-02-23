package com.origamilabs.orii.notification.listeners

/**
 * Interface pour écouter la réception d'appels entrants.
 */
interface OnIncomingCallReceivedListener {
    /**
     * Appelée lors de la réception d'un appel entrant.
     *
     * @param packageName Le nom du package de l'application source de l'appel.
     */
    fun onIncomingCallReceived(packageName: String)
}
