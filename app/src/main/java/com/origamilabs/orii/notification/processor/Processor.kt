package com.origamilabs.orii.notification.processor

/**
 * Classe abstraite de base pour le traitement des notifications.
 *
 * @property tickerText Le texte du ticker (peut être nul).
 * @property title Le titre de la notification (non nul).
 * @property text Le texte complet de la notification (peut être nul).
 * @property textLines Les lignes de texte additionnelles (peuvent être nulles).
 */
abstract class Processor(
    protected var tickerText: String?,
    protected var title: String,
    protected var text: String?,
    protected var textLines: Array<CharSequence>?
) {
    /**
     * Retourne le message à afficher, formaté selon les règles spécifiques à l'implémentation.
     */
    abstract fun getMessage(): String

    /**
     * Retourne l'expéditeur associé à la notification.
     */
    abstract fun getSender(): String

    /**
     * Indique si la notification est issue d'une conversation de groupe.
     */
    protected abstract fun isGroupChat(): Boolean
}
