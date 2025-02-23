package com.origamilabs.orii.notification.processor.whatsapp

import com.origamilabs.orii.notification.processor.Processor

/**
 * Classe de base pour le traitement des notifications WhatsApp.
 *
 * Cette classe est destinée à être étendue par des processeurs spécifiques à chaque langue (ex. : [EnglishProcessor], [ChineseProcessor], etc.).
 *
 * @param tickerText Le texte du ticker (peut être nul).
 * @param title Le titre de la notification (non nul).
 * @param text Le texte complet de la notification (peut être nul).
 * @param textLines Les lignes supplémentaires de la notification (peuvent être nulles).
 */
open class WhatsAppProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : Processor(tickerText, title, text, textLines) {

    override fun getMessage(): String? = null

    override fun getSender(): String? = null

    override fun isGroupChat(): Boolean = false
}
