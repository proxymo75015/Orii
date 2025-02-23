package com.origamilabs.orii.ui.tutorial.phase.three

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.FragmentUnscrewBinding
import com.origamilabs.orii.ui.tutorial.TutorialFragment

class UnscrewFragment : TutorialFragment() {

    private lateinit var binding: FragmentUnscrewBinding

    companion object {
        @JvmStatic
        fun newInstance(): UnscrewFragment = UnscrewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_unscrew, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Récupération du SimpleDraweeView depuis le layout
        val gifView = binding.root.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found in layout")
        // Création d'une ImageRequest pour la ressource R.raw.unscrew
        val imageRequest: ImageRequest = ImageRequestBuilder
            .newBuilderWithResourceId(R.raw.unscrew)
            .build()
        // Configuration du contrôleur Fresco avec l'URI de l'image et auto-play activé
        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(imageRequest.sourceUri)
            .setAutoPlayAnimations(true)
            .build()
        gifView.controller = controller
    }
}
