package com.origamilabs.orii.ui.main.home.update.batteryalert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.main.home.update.UpdateFragment

/**
 * Fragment affichant l'alerte de batterie.
 *
 * Ce fragment hérite de [UpdateFragment] et définit un tag spécifique (ici "2")
 * ainsi que le comportement des boutons « retour » et « retry ».
 */
class BatteryAlertFragment : UpdateFragment() {

    private lateinit var viewModel: BatteryAlertViewModel

    companion object {
        fun newInstance(): BatteryAlertFragment = BatteryAlertFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Définit le tag du fragment pour la journalisation ou autres usages
        setFragmentTag("2")
        return inflater.inflate(R.layout.battery_alert_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupère le ViewModel associé à ce fragment
        viewModel = ViewModelProvider(this).get(BatteryAlertViewModel::class.java)
        
        // Configure le bouton de retour pour naviguer vers l'écran précédent
        view?.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Configure le bouton "retry" pour lancer une mise à jour si la batterie est suffisante
        view?.findViewById<Button>(R.id.retry_button)?.setOnClickListener {
            if (viewModel.canUpdateWithCurrentBatteryLevel()) {
                findNavController().navigate(R.id.action_batteryAlertFragment_to_updatingFragment)
            }
        }
    }
}
