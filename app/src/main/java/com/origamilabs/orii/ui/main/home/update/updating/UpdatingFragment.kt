package com.origamilabs.orii.ui.main.home.update.updating

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.UpdatingFragmentBinding
import com.origamilabs.orii.ui.main.home.update.UpdateFragment

/**
 * Fragment affichant l'écran de mise à jour.
 *
 * Ce fragment hérite de [UpdateFragment] et utilise DataBinding pour gonfler son layout.
 * Il récupère le [UpdatingViewModel] via [ViewModelProvider] et lance la mise à jour via
 * la méthode [UpdatingViewModel.startUpdate()]. Des écouteurs sont ajoutés aux boutons
 * pour arrêter la mise à jour et revenir en arrière.
 */
class UpdatingFragment : UpdateFragment() {

    private lateinit var binding: UpdatingFragmentBinding
    private lateinit var viewModel: UpdatingViewModel

    companion object {
        private const val TAG = "UpdatingFragment"
        fun newInstance(): UpdatingFragment = UpdatingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Définition du tag du fragment (ici "3")
        setFragmentTag("3")
        binding = DataBindingUtil.inflate(inflater, R.layout.updating_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        binding.lifecycleOwner = viewLifecycleOwner

        // Récupération du ViewModel associé à ce fragment
        viewModel = ViewModelProvider(this).get(UpdatingViewModel::class.java)
        binding.updatingViewModel = viewModel

        // Démarre la mise à jour
        viewModel.startUpdate()

        // Configuration du bouton "back" : arrête la mise à jour et navigue en arrière
        binding.root.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            viewModel.stopUpdate()
            NavHostFragment.findNavController(this).navigateUp()
        }

        // Configuration du bouton "return" : ferme l'activité
        binding.root.findViewById<TextView>(R.id.return_text_view)?.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopUpdate()
    }
}
