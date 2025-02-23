package com.origamilabs.orii.ui.tutorial.phase.zero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import kotlin.TypeCastException

class InitialFragment : TutorialFragment(), Animation.AnimationListener {

    companion object {
        private const val TAG = "InitialFragment"
        @JvmStatic
        fun newInstance(): InitialFragment = InitialFragment()
    }

    private lateinit var animBlink: Animation
    private lateinit var viewModel: InitialViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.initial_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InitialViewModel::class.java)
        animBlink = AnimationUtils.loadAnimation(context, R.anim.three_times_blink_anim)
        animBlink.setAnimationListener(this)

        // Configure le texte du TextView "new_login_text_view" avec le nom d'utilisateur
        val newLoginTextView = view?.findViewById<TextView>(R.id.new_login_text_view)
            ?: throw NullPointerException("new_login_text_view not found in layout")
        newLoginTextView.text = getString(R.string.tutorial_new_login_text, viewModel.username)
    }

    override fun onPageSelected() {
        super.onPageSelected()
        val initiatingText = view?.findViewById<TextView>(R.id.initiating_text)
            ?: throw NullPointerException("initiating_text not found in layout")
        initiatingText.startAnimation(animBlink)
    }

    override fun onAnimationEnd(animation: Animation?) {
        // Lorsque l'animation se termine, navigue vers la page suivante du tutoriel
        val act = activity ?: throw TypeCastException("null cannot be cast to non-null type TutorialActivity")
        (act as TutorialActivity).navigateToNext()
    }

    override fun onAnimationRepeat(animation: Animation?) {
        // Pas d'action nécessaire pendant la répétition de l'animation
    }

    override fun onAnimationStart(animation: Animation?) {
        // Pas d'action nécessaire au démarrage de l'animation
    }
}
