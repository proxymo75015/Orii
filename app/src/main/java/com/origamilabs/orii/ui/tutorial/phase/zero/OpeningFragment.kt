package com.origamilabs.orii.ui.tutorial.phase.zero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import kotlin.TypeCastException

class OpeningFragment : TutorialFragment() {

    companion object {
        private const val TAG = "OpeningFragment"
        @JvmStatic
        fun newInstance(): OpeningFragment = OpeningFragment()
    }

    private lateinit var viewModel: InitialViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.opening_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupération du ViewModel
        viewModel = ViewModelProviders.of(this).get(InitialViewModel::class.java)

        // Mise à jour du texte de bienvenue avec le nom d'utilisateur
        val welcomeTextView = view?.findViewById<TextView>(R.id.welcome_text_view)
            ?: throw NullPointerException("welcome_text_view not found")
        welcomeTextView.text = getString(R.string.tutorial_welcome_text, viewModel.getUsername())

        // Bouton "Pair to Orii later" : navigue vers la page suivante
        view?.findViewById<Button>(R.id.pair_to_orii_later_button)?.setOnClickListener {
            activity?.runOnUiThread {
                val act = activity ?: throw TypeCastException("Activity is null")
                (act as TutorialActivity).navigateToNext()
            }
        }

        // Bouton "I'm ready" : navigue vers la page initiale
        view?.findViewById<Button>(R.id.im_ready_button)?.setOnClickListener {
            activity?.runOnUiThread {
                val act = activity ?: throw TypeCastException("Activity is null")
                (act as TutorialActivity).navigateToInitial()
            }
        }
    }
}
