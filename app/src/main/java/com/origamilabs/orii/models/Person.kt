package com.origamilabs.orii.models

/**
 * Représente un contact (personne) pour les alertes.
 *
 * @property pid L'identifiant unique du contact.
 * @property personName Le nom complet du contact.
 * @property ledColor La couleur LED associée au contact.
 * @property vibration Le paramètre de vibration associé au contact.
 */
data class Person(
    val pid: String,
    val personName: String,
    var ledColor: Int,
    var vibration: Int
)