package com.origamilabs.orii.ui.tutorial.phase.four

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

class RotateFragment : TutorialFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): RotateFragment = RotateFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_rotate, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val gifView = view?.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found in layout")
        val controllerBuilder: PipelineDraweeControllerBuilder = Fresco.newDraweeControllerBuilder()
        val imageRequest: ImageRequest = ImageRequestBuilder
            .newBuilderWithResourceId(R.raw.rotate)
            .build()
        controllerBuilder.setUri(imageRequest.sourceUri)
            .setAutoPlayAnimations(true)
        gifView.controller = controllerBuilder.build()
    }
}
