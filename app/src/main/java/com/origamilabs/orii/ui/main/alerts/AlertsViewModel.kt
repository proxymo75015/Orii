package com.origamilabs.orii.ui.main.alerts

import androidx.lifecycle.ViewModel
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.Application
import java.util.ArrayList

/**
 * ViewModel pour gérer la logique des alertes.
 *
 * Ce ViewModel fournit des méthodes pour ajouter un contact (personne) et pour obtenir
 * les listes d'alertes correspondant aux contacts et aux applications.
 */
class AlertsViewModel : ViewModel() {

    /**
     * Ajoute un contact à la liste des personnes disponibles.
     * La mise à jour est effectuée en arrière-plan via [AppManager.runQueryOnBackground].
     *
     * @param person Le contact à ajouter.
     */
    fun addPerson(person: Application) {
        AppManager.availablePeople.add(person)
        AppManager.runQueryOnBackground {
            AppManager.database.personDao().insertOrUpdate(person)
        }
    }

    /**
     * Récupère la liste des alertes correspondant aux contacts disponibles.
     *
     * @return Une [ArrayList] d'objets [AlertItem] construits à partir de la liste des contacts.
     */
    fun getPeopleList(): ArrayList<AlertItem> {
        val list = ArrayList<AlertItem>()
        val availablePeople: ArrayList<Application> = AppManager.availablePeople
        if (availablePeople.isNotEmpty()) {
            for (person in availablePeople) {
                list.add(AlertItem(person))
            }
        }
        return list
    }

    /**
     * Récupère la liste des alertes correspondant aux applications disponibles.
     *
     * @return Une [ArrayList] d'objets [AlertItem] construits à partir de la liste des applications.
     */
    fun getAppsList(): ArrayList<AlertItem> {
        val list = ArrayList<AlertItem>()
        for (app in AppManager.availableApps) {
            list.add(AlertItem(app))
        }
        return list
    }
}
