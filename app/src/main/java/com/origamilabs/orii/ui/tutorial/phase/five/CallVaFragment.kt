package com.origamilabs.orii.ui.tutorial.phase.five

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment

class CallVaFragment : TutorialFragment() {

    companion object {
        private const val TAG = "CallVaFragment"

        @JvmStatic
        fun newInstance(): CallVaFragment = CallVaFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_call_va, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupère la vue contenant le GIF
        val gifView = view?.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found")
        // Création du contrôleur Fresco pour afficher l'animation GIF
        val controllerBuilder: PipelineDraweeControllerBuilder = Fresco.newDraweeControllerBuilder()
        val imageRequest: ImageRequest = ImageRequestBuilder
            .newBuilderWithResourceId(R.raw.voice_assistant)
            .build()
        controllerBuilder.setUri(imageRequest.sourceUri)
            .setAutoPlayAnimations(true)
        gifView.controller = controllerBuilder.build()
    }

    /**
     * Méthode permettant de naviguer vers la page suivante du tutoriel.
     */
    private fun navigateToNext() {
        Log.d(TAG, "navigateToNext")
        Log.d(TAG, "activity: $activity")
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("null cannot be cast to non-null type TutorialActivity")
            (act as TutorialActivity).navigateToNext()
        }
    }
}
