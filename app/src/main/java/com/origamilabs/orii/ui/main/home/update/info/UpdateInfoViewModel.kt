package com.origamilabs.orii.ui.main.home.update.info

import androidx.lifecycle.ViewModel
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.FirmwareVersionInfo

/**
 * ViewModel pour afficher les informations de mise à jour.
 *
 * Il fournit les textes des corrections de bugs et des nouvelles fonctionnalités
 * à partir des données récupérées via [AppManager].
 */
class UpdateInfoViewModel : ViewModel() {

    /**
     * Retourne le texte des corrections de bugs.
     * @return Le texte des bug fixes ou null si les informations ne sont pas disponibles.
     */
    fun getBugFixesText(): CharSequence? {
        val firmwareVersionInfo: FirmwareVersionInfo? = AppManager.INSTANCE.getFirmwareVersionInfo()
        return firmwareVersionInfo?.bugFixes
    }

    /**
     * Retourne le texte des nouvelles fonctionnalités.
     * @return Le texte des nouvelles fonctionnalités ou null si les informations ne sont pas disponibles.
     */
    fun getNewFeaturesText(): CharSequence? {
        val firmwareVersionInfo: FirmwareVersionInfo? = AppManager.INSTANCE.getFirmwareVersionInfo()
        return firmwareVersionInfo?.newFeatures
    }
}
