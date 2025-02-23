package com.origamilabs.orii.models.`enum`

import com.origamilabs.orii.R

/**
 * Enumération représentant les actions de commande personnalisée.
 *
 * @property resId L'identifiant de la ressource associée à l'action, généralement utilisé pour afficher
 * un libellé textuel dans l'interface utilisateur.
 */
enum class CustomCommandAction(val resId: Int) {
    WEB_HOOK(R.string.dialog_setting_gesture_custom_command_option_0),
    DO_NOT_DISTURB_MODE(R.string.dialog_setting_gesture_custom_command_option_1),
    TIME_READOUT(R.string.dialog_setting_gesture_custom_command_option_2),
    FLASHLIGHT_SWITCH(R.string.dialog_setting_gesture_custom_command_option_3),
    SCREEN_ON_OFF(R.string.dialog_setting_gesture_custom_command_option_4),
    CALENDAR_READOUT(R.string.dialog_setting_gesture_custom_command_option_5)
}
