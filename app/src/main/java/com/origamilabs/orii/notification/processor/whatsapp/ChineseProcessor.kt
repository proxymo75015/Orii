package com.origamilabs.orii.notification.processor.whatsapp

/**
 * Processor pour les notifications WhatsApp en chinois.
 *
 * Extrait l'expéditeur du ticker en recherchant le marqueur "来自" (signifiant "de").
 * Le message est extrait de la même manière que pour un chat individuel ou de groupe.
 */
class ChineseProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : WhatsAppProcessor(tickerText, title, text, textLines) {

    override fun getSender(): String {
        val ticker = tickerText ?: return ""
        val marker = "来自"
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
        // S'il y a le terme "群聊" (group chat), on considère qu'il s'agit d'un chat de groupe.
        if (textVal.contains("群聊")) return true
        val ticker = tickerText ?: throw NullPointerException("tickerText is null")
        val replaced = ticker.replace(getTitle(), "")
        return replaced.length > 2 && replaced[replaced.length - 2] == '@'
    }
}
