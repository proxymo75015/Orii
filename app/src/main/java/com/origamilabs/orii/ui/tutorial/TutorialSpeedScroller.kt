package com.origamilabs.orii.ui.tutorial

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller
import kotlin.math.roundToInt

class TutorialSpeedScroller(context: Context, interpolator: Interpolator) : Scroller(context, interpolator) {

    private var mScrollSpeed: Double = 1.0

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, (duration * mScrollSpeed).roundToInt())
    }

    fun setScrollSpeed(scrollSpeed: Double) {
        mScrollSpeed = scrollSpeed
    }
}
