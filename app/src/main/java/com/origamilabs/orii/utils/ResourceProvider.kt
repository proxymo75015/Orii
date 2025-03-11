package com.origamilabs.orii.utils

import android.content.Context import androidx.core.content.ContextCompat

class ResourceProvider(private val context: Context) {

    // Accès de base aux ressources via l'ID
    fun getString(resId: Int): String {
        require(resId != 0) { "L'ID de ressource est invalide (0)." }
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        require(resId != 0) { "L'ID de ressource est invalide (0)." }
        return context.getString(resId, *formatArgs)
    }

    fun getDrawable(resId: Int) = ContextCompat.getDrawable(context, resId)
    fun getColor(resId: Int) = ContextCompat.getColor(context, resId)

    // Récupération dynamique de l'ID de la ressource en se basant sur son nom défini dans le XML.
    // Cette méthode permet de ne pas dépendre directement du fichier R.
    val homeWelcome: String
        get() {
            val id = context.resources.getIdentifier("home_welcome", "string", context.packageName)
            require(id != 0) { "La ressource 'home_welcome' n'a pas été trouvée." }
            return context.getString(id)
        }

    val homeSoundTestPlay: String
        get() {
            val id = context.resources.getIdentifier("home_sound_test_play", "string", context.packageName)
            require(id != 0) { "La ressource 'home_sound_test_play' n'a pas été trouvée." }
            return context.getString(id)
        }

    val homeSoundTestStop: String
        get() {
            val id = context.resources.getIdentifier("home_sound_test_stop", "string", context.packageName)
            require(id != 0) { "La ressource 'home_sound_test_stop' n'a pas été trouvée." }
            return context.getString(id)
        }

    fun helpFirmware(version: String): String {
        val id = context.resources.getIdentifier("help_firmware", "string", context.packageName)
        require(id != 0) { "La ressource 'help_firmware' n'a pas été trouvée." }
        return context.getString(id, version)
    }
}
