package com.origamilabs.orii.data

import android.content.Context
import android.provider.ContactsContract

/**
 * Remplace l’utilisation de Firebase Auth par un profil local.
 * Récupère éventuellement le nom du propriétaire du téléphone.
 */
class DeviceUserRepository(private val context: Context) {

    fun getLocalUserName(): String {
        // Tente d’obtenir le nom du profil utilisateur Android ("Moi")
        val projection = arrayOf(ContactsContract.Profile.DISPLAY_NAME)
        context.contentResolver.query(ContactsContract.Profile.CONTENT_URI, projection, null, null, null).use { c ->
            if (c != null && c.moveToFirst()) {
                val name = c.getString(0)
                if (!name.isNullOrEmpty()) {
                    return name
                }
            }
        }
        // Si non disponible, on peut utiliser le nom du compte Google (s’il existe) ou un nom générique
        // (Cette partie peut être adaptée selon les besoins)
        return "Utilisateur"
    }
}
