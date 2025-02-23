package com.origamilabs.orii.utils

/**
 * Fournit des fonctions utilitaires pour la validation.
 */
object Validator {

    /**
     * Vérifie si l'adresse e-mail fournie est valide selon le pattern défini.
     *
     * @param emailAddress L'adresse e-mail à valider.
     * @return `true` si l'adresse e-mail correspond au pattern, `false` sinon.
     */
    fun isEmailValid(emailAddress: String): Boolean {
        return Regex("^[_a-z0-9-]+([.][_a-z0-9-]+)*@[a-z0-9-]+([.][a-z0-9-]+)*$").matches(emailAddress)
    }
}
