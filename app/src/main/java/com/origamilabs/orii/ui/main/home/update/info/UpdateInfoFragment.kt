package com.origamilabs.orii.ui.main.home.update.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.facebook.appevents.AppEventsConstants
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.main.home.update.UpdateFragment
import androidx.lifecycle.ViewModelProvider

/**
 * Fragment affichant les informations de mise à jour.
 *
 * Ce fragment hérite de [UpdateFragment] et affiche des informations telles que les
 * nouvelles fonctionnalités et les corrections de bugs. Il offre également un bouton
 * de retour et un bouton de continuité pour naviguer vers le fragment de rappel.
 */
class UpdateInfoFragment : UpdateFragment() {

    private lateinit var viewModel: UpdateInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Définit le tag du fragment (ici, la valeur "no" issue de AppEventsConstants.EVENT_PARAM_VALUE_NO)
        setFragmentTag(AppEventsConstants.EVENT_PARAM_VALUE_NO)
        // Gonfle le layout du fragment
        return inflater.inflate(R.layout.update_info_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupération du ViewModel associé à ce fragment
        viewModel = ViewModelProvider(this).get(UpdateInfoViewModel::class.java)

        // Mise à jour du contenu des TextView avec les textes récupérés depuis le ViewModel
        view?.findViewById<TextView>(R.id.bug_fixes_text_view)?.text = viewModel.getBugFixesText()
        view?.findViewById<TextView>(R.id.new_features_text_view)?.text = viewModel.getNewFeaturesText()

        // Configuration du bouton "back" pour fermer l'activité
        view?.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            activity?.finish()
        }

        // Configuration du bouton "continue" pour naviguer vers le fragment de rappel
        view?.findViewById<TextView>(R.id.continue_button)?.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_updateInfoFragment_to_reminderFragment)
        }
    }
}
