package com.origamilabs.orii.ui.tutorial.phase.one

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.origamilabs.orii.R
import com.origamilabs.orii.core.bluetooth.manager.ConnectionManager
import com.origamilabs.orii.databinding.ConnectionFragmentBinding
import com.origamilabs.orii.ui.common.BtLocationEnableDialogFragment
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import kotlin.TypeCastException

class ConnectionFragment : TutorialFragment() {

    companion object {
        private const val TAG = "ConnectionFragment"
        @JvmStatic
        fun newInstance() = ConnectionFragment()
    }

    private lateinit var binding: ConnectionFragmentBinding
    private lateinit var viewModel: ConnectionViewModel
    private lateinit var animBlink: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.connection_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConnectionViewModel::class.java)
        viewModel.init(this)
        binding.viewModel = viewModel

        // Charge l'animation de clignotement
        animBlink = AnimationUtils.loadAnimation(context, R.anim.circulator_blink_anim)
    }

    override fun onPageSelected() {
        super.onPageSelected()
        // Démarre l'animation sur la vue identifiée par "blink_light_image_view"
        val blinkLight = view?.findViewById<ImageView>(R.id.blink_light_image_view)
            ?: throw NullPointerException("blink_light_image_view not found")
        blinkLight.startAnimation(animBlink)

        // Vérifie la connexion
        val connectionManager = ConnectionManager.getInstance()
        if (connectionManager.isOriiConnected()) {
            navigateToNext()
        } else {
            showDialog()
        }
    }

    private fun showDialog() {
        // Crée et configure la boîte de dialogue pour activer Bluetooth et GPS
        val dialog = BtLocationEnableDialogFragment.newInstance()
        if (dialog != null) {
            dialog.setOnDialogDismissListener(ConnectionFragmentShowDialogListener(this, dialog))
            val fm = fragmentManager ?: throw NullPointerException("FragmentManager is null")
            dialog.show(fm, "enable_bt_gps")
        }
    }

    fun navigateToNext() {
        Log.d(TAG, "navigateToNext")
        Log.d(TAG, "activity $activity")
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("null cannot be cast to non-null type TutorialActivity")
            (act as TutorialActivity).navigateToNext()
        }
    }
}
