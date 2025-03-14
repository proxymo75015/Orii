package com.origamilabs.orii.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

/**
 * Fournisseur de ressources pour accéder aux ressources de l'application sans dépendance directe sur R.
 */
class ResourceProvider(val context: Context) {

    // Accès de base aux ressources via l'ID
    fun getString(resId: Int): String {
        require(resId != 0) { "L'ID de ressource est invalide (0)." }
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        require(resId != 0) { "L'ID de ressource est invalide (0)." }
        return context.getString(resId, *formatArgs)
    }

    fun getDrawable(resId: Int): Drawable? = ContextCompat.getDrawable(context, resId)
    fun getColor(resId: Int) = ContextCompat.getColor(context, resId)

    // Accès aux ressources par nom pour éviter une dépendance directe à R
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

    val tabHome: String
        get() {
            val id = context.resources.getIdentifier("tab_home", "string", context.packageName)
            require(id != 0) { "La ressource 'tab_home' n'a pas été trouvée." }
            return context.getString(id)
        }

    val tabAlerts: String
        get() {
            val id = context.resources.getIdentifier("tab_alerts", "string", context.packageName)
            require(id != 0) { "La ressource 'tab_alerts' n'a pas été trouvée." }
            return context.getString(id)
        }

    val tabSettings: String
        get() {
            val id = context.resources.getIdentifier("tab_settings", "string", context.packageName)
            require(id != 0) { "La ressource 'tab_settings' n'a pas été trouvée." }
            return context.getString(id)
        }

    val tabHelp: String
        get() {
            val id = context.resources.getIdentifier("tab_help", "string", context.packageName)
            require(id != 0) { "La ressource 'tab_help' n'a pas été trouvée." }
            return context.getString(id)
        }

    // ID du container pour la gestion des fragments
    val containerId: Int
        get() {
            val id = context.resources.getIdentifier("container", "id", context.packageName)
            require(id != 0) { "La ressource 'container' n'a pas été trouvée." }
            return id
        }

    val eAudioDemoNonna: Int
        get() {
            val id = context.resources.getIdentifier("e_audio_demo_nonna", "raw", context.packageName)
            require(id != 0) { "La ressource 'e_audio_demo_nonna' n'a pas été trouvée." }
            return id
        }

    val ttsTestMessage: String
        get() {
            val id = context.resources.getIdentifier("tts_test_message", "string", context.packageName)
            require(id != 0) { "La ressource 'tts_test_message' n'a pas été trouvée." }
            return context.getString(id)
        }

    // Propriétés pour rendre AppService indépendant de R
    val notificationConnectionChannelId: String
        get() {
            val id = context.resources.getIdentifier("notification_connection_channel_id", "string", context.packageName)
            require(id != 0) { "La ressource 'notification_connection_channel_id' n'a pas été trouvée." }
            return context.getString(id)
        }

    val notificationBatteryChannelId: String
        get() {
            val id = context.resources.getIdentifier("notification_battery_channel_id", "string", context.packageName)
            require(id != 0) { "La ressource 'notification_battery_channel_id' n'a pas été trouvée." }
            return context.getString(id)
        }

    val notificationTitleBatteryLow: String
        get() {
            val id = context.resources.getIdentifier("notification_title_battery_low", "string", context.packageName)
            require(id != 0) { "La ressource 'notification_title_battery_low' n'a pas été trouvée." }
            return context.getString(id)
        }

    val notificationTextBatteryLow: String
        get() {
            val id = context.resources.getIdentifier("notification_text_battery_low", "string", context.packageName)
            require(id != 0) { "La ressource 'notification_text_battery_low' n'a pas été trouvée." }
            return context.getString(id)
        }

    val notificationConnectionChannelName: String
        get() {
            val id = context.resources.getIdentifier("notification_connection_channel_name", "string", context.packageName)
            require(id != 0) { "La ressource 'notification_connection_channel_name' n'a pas été trouvée." }
            return context.getString(id)
        }

    val appName: String
        get() {
            val id = context.resources.getIdentifier("app_name", "string", context.packageName)
            require(id != 0) { "La ressource 'app_name' n'a pas été trouvée." }
            return context.getString(id)
        }

    val icStatusbar: Drawable?
        get() {
            val id = context.resources.getIdentifier("ic_statusbar", "drawable", context.packageName)
            require(id != 0) { "La ressource 'ic_statusbar' n'a pas été trouvée." }
            return ContextCompat.getDrawable(context, id)
        }

    // Nouvelle propriété pour l'icône de notification
    val notificationIcon: Int
        get() {
            val id = context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
            require(id != 0) { "La ressource 'ic_notification' n'a pas été trouvée." }
            return id
        }
}
