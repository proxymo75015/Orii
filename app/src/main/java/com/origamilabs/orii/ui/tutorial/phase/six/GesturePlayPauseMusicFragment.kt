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

class GesturePlayPauseMusicFragment : TutorialFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): GesturePlayPauseMusicFragment = GesturePlayPauseMusicFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gesture_play_pause_music, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val gifView = view?.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found in layout")
        val imageRequest: ImageRequest = ImageRequestBuilder
            .newBuilderWithResourceId(R.raw.gesture_play_pause_music)
            .build()
        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(imageRequest.sourceUri)
            .setAutoPlayAnimations(true)
            .build()
        gifView.controller = controller
    }
}
