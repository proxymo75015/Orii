package com.origamilabs.orii.notification.processor.whatsapp

/**
 * Processor pour les notifications WhatsApp en anglais.
 *
 * Extrait l'expéditeur et le message de la notification.
 */
class EnglishProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : WhatsAppProcessor(tickerText, title, text, textLines) {

    override fun getSender(): String {
        // Si le ticker est nul, on retourne une chaîne vide.
        val ticker = tickerText ?: return ""
        // Recherche de "Message from" et extraction de la partie suivante (en ajoutant 12 caractères)
        val index = ticker.indexOf("Message from")
        val startIndex = if (index != -1) index + 12 else 0
        return ticker.substring(startIndex).trim().toString()
    }

    override fun getMessage(): String {
        val textVal = text ?: throw NullPointerException("text is null")
        val lines = textLines

        return if (lines == null) {
            // Aucun textLines disponible :
            if (isGroupChat()) {
                // Pour les chats de groupe, on utilise le sender pour découper le texte
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
        } else {
            // Si textLines est disponible, on examine d'abord si le texte contient "chats"
            if (textVal.contains("chats")) {
                val parts = lines.last().toString().split(":")
                if (parts.size > 1) {
                    return parts[1].trim().toString()
                }
            }
            // Sinon, si c'est un chat de groupe, on procède de la même manière
            if (isGroupChat()) {
                val parts = lines.last().toString().split(":")
                if (parts.size > 1) {
                    return parts[1].trim().toString()
                } else {
                    return textVal
                }
            }
            // Par défaut, on retourne le dernier élément de textLines
            lines.last().toString()
        }
    }

    override fun isGroupChat(): Boolean {
        val textVal = text ?: throw NullPointerException("text is null")
        if (textVal.contains("chats")) return false
        val ticker = tickerText ?: throw NullPointerException("tickerText is null")
        val replaced = ticker.replace(getTitle(), "")
        return replaced.length > 2 && replaced[replaced.length - 2] == '@'
    }
}
