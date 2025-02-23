package com.origamilabs.orii.ui.main.home.update.reminder

import androidx.lifecycle.ViewModel
import com.origamilabs.orii.manager.AppManager

/**
 * ViewModel pour le fragment de rappel.
 *
 * Il fournit une méthode pour vérifier si la mise à jour peut être lancée en fonction du niveau de batterie.
 */
class ReminderViewModel : ViewModel() {
    fun canUpdateWithCurrentBatteryLevel(): Boolean {
        return AppManager.INSTANCE.getBatteryLevel() > 1
    }
}
