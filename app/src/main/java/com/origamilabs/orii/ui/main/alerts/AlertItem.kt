package com.origamilabs.orii.ui.main.alerts

/**
 * Représente un élément d'alerte contenant des données ainsi qu'un état d'extension.
 *
 * @property data Les données associées à l'alerte.
 * @property expanded Indique si l'alerte est étendue ou non. La valeur par défaut est false.
 */
data class AlertItem(
    val data: Any,
    var expanded: Boolean = false
) {

    /**
     * Définit le type de l'élément d'alerte.
     */
    enum class ItemType {
        PEOPLE_HEADER,
        PEOPLE,
        APPS_HEADER,
        APPS
    }
}
