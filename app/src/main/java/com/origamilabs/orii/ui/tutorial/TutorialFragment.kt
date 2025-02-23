package com.origamilabs.orii.ui.tutorial

import androidx.fragment.app.Fragment
import com.origamilabs.orii.manager.AnalyticsManager

open class TutorialFragment : Fragment() {

    /**
     * Méthode appelée lorsqu'une page devient active.
     * Peut être surchargée par les sous-classes.
     */
    open fun onPageSelected() {
        // Implémentation par défaut (vide)
    }

    /**
     * Méthode appelée lorsqu'une page n'est plus sélectionnée.
     * Peut être surchargée par les sous-classes.
     */
    open fun onPageUnselected() {
        // Implémentation par défaut (vide)
    }

    override fun onResume() {
        super.onResume()
        // Récupération et vérification du tag du fragment
        val fragmentTag = tag ?: throw NullPointerException("tag!!")
        // On découpe le tag sur le caractère ':' et on récupère la dernière partie
        val parts = fragmentTag.split(":")
        val pageIndex = parts.last().toInt()
        // Log de la progression du tutoriel via AnalyticsManager
        AnalyticsManager.logTutorialFlow(requireActivity(), pageIndex)
    }
}
