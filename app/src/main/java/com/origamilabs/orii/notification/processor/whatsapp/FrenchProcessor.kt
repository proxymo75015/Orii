package com.origamilabs.orii.notification.processor.whatsapp

/**
 * Processor pour les notifications WhatsApp en français.
 *
 * Extrait l'expéditeur du ticker en recherchant le marqueur "Message de" (signifiant "message de").
 * Le traitement du message est similaire à celui des autres processeurs.
 */
class FrenchProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : WhatsAppProcessor(tickerText, title, text, textLines) {

    override fun getSender(): String {
        val ticker = tickerText ?: return ""
        val marker = "Message de"
        val index = ticker.indexOf(marker)
        val startIndex = if (index != -1) index + marker.length else 0
        return ticker.substring(startIndex).trim().toString()
    }

    override fun getMessage(): String {
        val textVal = text ?: throw NullPointerException("text is null")
        return if (isGroupChat()) {
            val sender = getSender()
            val atIndex = sender.indexOf("@")
            val trimmedSender = if (atIndex != -1) sender.substring(0, atIndex).trim() else sender.trim()
            if (textVal.length > trimmedSender.length) {
                textVal.substring(trimmedSender.length + 1).trim().toString()
            } else {
                textVal
            }
        } else {
            textVal
        }
    }

    override fun isGroupChat(): Boolean {
        val textVal = text ?: throw NullPointerException("text is null")
        // Si le texte contient "groupe", on considère qu'il s'agit d'un chat de groupe.
        if (textVal.contains("groupe", ignoreCase = true)) return true
        val ticker = tickerText ?: throw NullPointerException("tickerText is null")
        val replaced = ticker.replace(getTitle(), "")
        return replaced.length > 2 && replaced[replaced.length - 2] == '@'
    }
}
