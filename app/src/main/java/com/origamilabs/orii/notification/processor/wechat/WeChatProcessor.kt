package com.origamilabs.orii.notification.processor.wechat

import com.origamilabs.orii.notification.processor.Processor
import kotlin.text.Regex
import kotlin.text.trim

/**
 * Processor pour les notifications WeChat.
 *
 * Pour les conversations de groupe, le message est supposé être préfixé par un nom d'expéditeur,
 * éventuellement suivi d'une mention entre crochets (ex : "[123]"), puis d'un séparateur ":".
 * Pour les messages individuels, le message est extrait en supprimant le préfixe constitué du nom de l'expéditeur suivi de ":".
 */
class WeChatProcessor(
    tickerText: String?,
    title: String,
    text: String?,
    textLines: Array<CharSequence>?
) : Processor(tickerText, title, text, textLines) {

    override fun getSender(): String {
        return if (isGroupChat()) {
            // Pour les chats de groupe, le texte complet contient le nom de l'expéditeur en premier,
            // éventuellement suivi d'une mention entre crochets. On extrait ce préfixe et on élimine
            // toute occurrence de [nombre].
            val txt = text ?: throw NullPointerException("text is null")
            val parts = Regex(":").split(txt)
            // Suppression des occurrences du motif "[nombre]"
            Regex("\\[\\d+\\]").replace(parts[0], "")
        } else {
            title
        }
    }

    override fun getMessage(): String {
        val txt = text ?: throw NullPointerException("text is null")
        return if (isGroupChat()) {
            // Pour les chats de groupe, le texte est découpé par ":" et le message correspond à la
            // concaténation de tous les segments situés après le premier.
            val parts = Regex(":").split(txt)
            if (parts.size < 2) {
                ""
            } else {
                parts.subList(1, parts.size).joinToString(":").trim().toString()
            }
        } else {
            // Pour un message individuel, on suppose que le texte commence par "sender:".
            val delimiter = "${getSender()}:"
            val parts = txt.split(delimiter)
            when (parts.size) {
                1 -> parts[0].trim().toString()
                2 -> parts[1].trim().toString()
                else -> ""
            }
        }
    }

    override fun isGroupChat(): Boolean {
        val txt = text ?: throw NullPointerException("text is null")
        val parts = Regex(":").split(txt)
        if (parts.size > 1) {
            // Si le texte est découpé par ":" et que le préfixe ne contient pas le titre suivi de ":",
            // alors il s'agit d'un chat de groupe.
            val parts2 = txt.split("${title}:")
            if (parts2.size == 1) {
                return true
            }
        }
        return false
    }
}
