package com.origamilabs.orii.ui.tutorial.phase.two

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import kotlin.TypeCastException

class MainMenuFragment : TutorialFragment() {

    companion object {
        private const val TAG = "MainMenuFragment"
        @JvmStatic
        fun newInstance(): MainMenuFragment = MainMenuFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main_menu, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Configuration des écouteurs de clic sur les CardView du menu
        view?.findViewById<CardView>(R.id.main_menu_find_the_fit_card_view)
            ?.setOnClickListener { navigateToFindTheFitPage() }
        view?.findViewById<CardView>(R.id.main_menu_find_the_sweet_spot_card_view)
            ?.setOnClickListener { navigateToFindTheSweetSpotPage() }
        view?.findViewById<CardView>(R.id.main_menu_message_readout_card_view)
            ?.setOnClickListener { navigateToMessageReadoutPage() }
        view?.findViewById<CardView>(R.id.main_menu_voice_assistant_card_view)
            ?.setOnClickListener { navigateToVoiceAssistantPage() }
        view?.findViewById<CardView>(R.id.main_menu_gesture_card_view)
            ?.setOnClickListener { navigateToGesturePage() }
    }

    private fun navigateToFindTheFitPage() {
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("Activity is null")
            (act as TutorialActivity).navigateToFindTheFit()
        }
    }

    private fun navigateToFindTheSweetSpotPage() {
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("Activity is null")
            (act as TutorialActivity).navigateToFindTheSweetSpot()
        }
    }

    private fun navigateToMessageReadoutPage() {
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("Activity is null")
            (act as TutorialActivity).navigateToMessageReadout()
        }
    }

    private fun navigateToVoiceAssistantPage() {
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("Activity is null")
            (act as TutorialActivity).navigateToVoiceAssistant()
        }
    }

    private fun navigateToGesturePage() {
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("Activity is null")
            (act as TutorialActivity).navigateToGesture()
        }
    }
}
