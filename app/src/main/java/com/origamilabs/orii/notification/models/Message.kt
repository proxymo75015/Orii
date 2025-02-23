package com.origamilabs.orii.notification.models

/**
 * Représente un message avec son contenu, son horodatage et le nom de l'application associée.
 *
 * @property messageContent Le contenu du message.
 * @property timestamp L'horodatage du message.
 * @property appName Le nom de l'application associée au message.
 */
data class Message(
    var messageContent: String,
    var timestamp: Int,
    var appName: String
)
