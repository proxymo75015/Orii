package com.origamilabs.orii.ui.tutorial

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.Interpolator
import androidx.viewpager.widget.ViewPager
import java.lang.reflect.Field
import kotlin.TypeCastException
import kotlin.math.roundToInt

class TutorialViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    companion object {
        private const val TAG = "TutorialViewPager"
    }

    // Instance de notre Scroller personnalisé
    private var mScroller: TutorialSpeedScroller? = null

    // Point de départ en X pour le swipe
    private var startX: Float = 0f

    /**
     * Enumération définissant la direction du swipe.
     */
    enum class SwipeDirection {
        ALL,
        LEFT,
        RIGHT,
        NONE
    }

    init {
        initScroller()
        setScrollerSpeed(2.0)
    }

    /**
     * Retourne le TutorialSpeedScroller utilisé.
     */
    fun getMScroller(): TutorialSpeedScroller {
        return mScroller ?: throw UninitializedPropertyAccessException("mScroller")
    }

    /**
     * Définit le TutorialSpeedScroller.
     */
    fun setMScroller(scroller: TutorialSpeedScroller) {
        mScroller = scroller
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (handleSwipe(ev)) super.onTouchEvent(ev) else false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (handleSwipe(ev)) super.onInterceptTouchEvent(ev) else false
    }

    /**
     * Initialise le Scroller personnalisé via réflexion afin de remplacer le mScroller de ViewPager.
     */
    private fun initScroller() {
        try {
            val scrollerField: Field = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            val interpolatorField: Field = ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolatorField.isAccessible = true
            val interpolatorObj = interpolatorField.get(null)
            val interpolator = interpolatorObj as? Interpolator
                ?: throw TypeCastException("null cannot be cast to non-null type Interpolator")
            mScroller = TutorialSpeedScroller(context, interpolator)
            scrollerField.set(this, mScroller)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    /**
     * Définit la vitesse du scroller personnalisé.
     */
    private fun setScrollerSpeed(speed: Double) {
        mScroller?.setScrollSpeed(speed) ?: throw UninitializedPropertyAccessException("mScroller")
    }

    /**
     * Gère le swipe en fonction de l'action de l'événement MotionEvent.
     * Retourne true si le swipe est autorisé, false sinon.
     */
    private fun handleSwipe(ev: MotionEvent): Boolean {
        return when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val swipeDirection = if (ev.x - startX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                // Ici, pour tous les cas, nous utilisons SwipeDirection.NONE comme pageDir
                val allowed = allowSwipe(swipeDirection, SwipeDirection.NONE)
                if (allowed) {
                    startX = ev.x
                }
                allowed
            }
            else -> true
        }
    }

    /**
     * Détermine si le swipe est autorisé.
     * Si pageDir est NONE, le swipe n'est pas autorisé.
     * Sinon, le swipe est autorisé si la direction courante correspond à pageDir ou si pageDir est ALL.
     */
    private fun allowSwipe(currentDir: SwipeDirection, pageDir: SwipeDirection): Boolean {
        if (pageDir == SwipeDirection.NONE) return false
        return pageDir == currentDir || pageDir == SwipeDirection.ALL
    }
}
