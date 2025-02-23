package com.origamilabs.orii.ui.main.home.update

import androidx.fragment.app.Fragment
import com.origamilabs.orii.manager.AnalyticsManager

/**
 * Fragment de mise à jour.
 *
 * Ce fragment possède un tag qui peut être utilisé pour la journalisation de la mise à jour OTA.
 */
class UpdateFragment : Fragment() {

    var fragmentTag: String = "-1"

    override fun onResume() {
        super.onResume()
        // Utilise requireActivity() pour obtenir une activité non nulle.
        AnalyticsManager.INSTANCE.logOtaFlow(requireActivity(), fragmentTag)
    }
}
