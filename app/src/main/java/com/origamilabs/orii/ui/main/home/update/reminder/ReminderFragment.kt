package com.origamilabs.orii.ui.main.home.update.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.facebook.appevents.AppEventsConstants
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.main.home.update.UpdateFragment

/**
 * Fragment affichant le rappel de mise à jour.
 *
 * Ce fragment hérite de [UpdateFragment] et définit son comportement en fonction du niveau de batterie.
 * Si le niveau de batterie est suffisant, il navigue vers l'écran de mise à jour,
 * sinon il affiche une alerte de batterie.
 */
class ReminderFragment : UpdateFragment() {

    private lateinit var viewModel: ReminderViewModel

    companion object {
        fun newInstance(): ReminderFragment = ReminderFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Définir le tag du fragment (ici "yes" issu de AppEventsConstants.EVENT_PARAM_VALUE_YES)
        setFragmentTag(AppEventsConstants.EVENT_PARAM_VALUE_YES)
        return inflater.inflate(R.layout.reminder_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        // Bouton de retour : ferme le fragment via la navigation ascendante
        view?.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        // Bouton de mise à jour : navigue vers l'écran approprié selon le niveau de batterie
        view?.findViewById<TextView>(R.id.update_text_view)?.setOnClickListener {
            if (viewModel.canUpdateWithCurrentBatteryLevel()) {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_reminderFragment_to_updatingFragment)
            } else {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_reminderFragment_to_batteryAlertFragment)
            }
        }
    }
}
