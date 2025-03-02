package com.origamilabs.orii.data

import android.content.Context
import android.provider.ContactsContract

/**
 * Remplace l’utilisation de Firebase Auth par un profil local.
 * Récupère éventuellement le nom du propriétaire du téléphone.
 */
class DeviceUserRepository(private val context: Context) {

    fun getLocalUserName(): String {
        val projection = arrayOf(ContactsContract.Profile.DISPLAY_NAME)
        context.contentResolver.query(ContactsContract.Profile.CONTENT_URI, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val name = cursor.getString(0)
                if (!name.isNullOrEmpty()) {
                    return name
                }
            }
        }
        return "Utilisateur"
    }
}
