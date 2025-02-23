package com.origamilabs.orii.models

/**
 * Représente les informations relatives à une version du firmware.
 *
 * @property versionNumber Numéro de version.
 * @property url URL associée à cette version.
 * @property remark Remarques sur cette version.
 * @property bugFixes Liste des corrections apportées.
 * @property newFeatures Liste des nouvelles fonctionnalités.
 */
data class FirmwareVersionInfo(
    val versionNumber: Int,
    val url: String,
    val remark: String,
    val bugFixes: String,
    val newFeatures: String
)
