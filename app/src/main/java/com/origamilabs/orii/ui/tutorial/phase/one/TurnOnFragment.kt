package com.origamilabs.orii.ui.tutorial.phase.one

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.TurnOnFragmentBinding
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import kotlin.TypeCastException

class TurnOnFragment : TutorialFragment() {

    companion object {
        private const val TAG = "TurnOnFragment"
        @JvmStatic
        fun newInstance(): TurnOnFragment = TurnOnFragment()
    }

    private lateinit var binding: TurnOnFragmentBinding
    private lateinit var viewModel: TurnOnViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.turn_on_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TurnOnViewModel::class.java)

        // Configure le SimpleDraweeView pour afficher l'animation GIF
        val gifView = view?.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found")
        val controllerBuilder: PipelineDraweeControllerBuilder = Fresco.newDraweeControllerBuilder()
        val imageRequest: ImageRequest = ImageRequestBuilder
            .newBuilderWithResourceId(R.raw.turn_on)
            .build()
        gifView.controller = controllerBuilder
            .setUri(imageRequest.sourceUri)
            .setAutoPlayAnimations(true)
            .build()

        // Configure le bouton "Next"
        view?.findViewById<Button>(R.id.next_text_button)?.setOnClickListener {
            navigateToNext()
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
