package com.origamilabs.orii.ui.main.home.update.batteryalert

import androidx.lifecycle.ViewModel
import com.origamilabs.orii.manager.AppManager

/**
 * ViewModel pour la gestion de l'alerte de batterie.
 *
 * La méthode [canUpdateWithCurrentBatteryLevel] retourne true si le niveau de batterie
 * (récupéré via [AppManager]) est supérieur à 1.
 */
class BatteryAlertViewModel : ViewModel() {
    fun canUpdateWithCurrentBatteryLevel(): Boolean {
        return AppManager.INSTANCE.getBatteryLevel() > 1
    }
}
