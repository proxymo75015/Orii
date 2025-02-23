package com.origamilabs.orii.models

/**
 * Représente un utilisateur.
 *
 * @property id L'identifiant unique de l'utilisateur.
 * @property email L'adresse email de l'utilisateur.
 * @property name Le nom complet de l'utilisateur.
 * @property token Le token d'authentification associé à l'utilisateur.
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val token: String
)
