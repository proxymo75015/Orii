package com.origamilabs.orii.ui.tutorial.phase.six

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.tutorial.TutorialFragment

class GestureAnglesFragment : TutorialFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): GestureAnglesFragment = GestureAnglesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gesture_angles, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupère le SimpleDraweeView dans le layout
        val gifView = view?.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found in layout")
        // Crée la requête d'image pour la ressource drawable "gesture_angles"
        val imageRequest: ImageRequest = ImageRequestBuilder
            .newBuilderWithResourceId(R.drawable.gesture_angles)
            .build()
        // Configure le contrôleur Fresco avec l'URI de l'image et l'option d'auto-play des animations
        val controller: PipelineDraweeControllerBuilder = Fresco.newDraweeControllerBuilder()
        controller.setUri(imageRequest.sourceUri)
            .setAutoPlayAnimations(true)
        gifView.controller = controller.build()
    }
}
