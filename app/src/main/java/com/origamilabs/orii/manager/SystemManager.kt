package com.origamilabs.orii.manager

import com.origamilabs.orii.notification.NotificationManager
import com.origamilabs.orii.controller.DeviceController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ce gestionnaire centralise les opérations système en combinant les fonctionnalités
 * de NotificationManager et DeviceController.
 */
@Singleton
class SystemManager @Inject constructor(
    private val notificationManager: NotificationManager,
    private val deviceController: DeviceController
) {

    /**
     * Envoie une notification et verrouille immédiatement l’écran.
     *
     * Cette méthode peut être utilisée, par exemple, pour alerter l’utilisateur
     * et sécuriser l’appareil en cas d’événement critique.
     */
    fun notifyAndLockScreen(title: String, message: String) {
        notificationManager.sendNotification(title, message)
        deviceController.switchScreenLock()
    }

    /**
     * Bascule la lampe torche et envoie une notification d’information.
     *
     * Une telle synergie peut être utile pour confirmer visuellement l’activation
     * ou la désactivation de la lampe torche.
     */
    fun toggleFlashlightWithNotification(title: String, message: String) {
        deviceController.switchFlashlight()
        notificationManager.sendNotification(title, message)
    }

    /**
     * Méthode d’exemple pour combiner plusieurs actions système.
     *
     * Vous pouvez ajouter d’autres méthodes pour intégrer d’autres scénarios
     * (ex. basculement du mode Ne pas déranger suivi d’une alerte, etc.)
     */
    fun performCombinedAction() {
        // Exemple : envoyer une notification, basculer le mode Ne pas déranger et verrouiller l’écran
        notificationManager.sendNotification("Attention", "Action système en cours")
        deviceController.switchDisturbMode()
        deviceController.switchScreenLock()
    }
}
