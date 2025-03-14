package com.origamilabs.orii.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.origamilabs.orii.R
import com.origamilabs.orii.services.MyInCallService
import timber.log.Timber

class CallScreenActivity : AppCompatActivity(), GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    private lateinit var gestureDetector: GestureDetectorCompat

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val TRIPLE_TAP_TIMEOUT = 600L // délai en ms pour distinguer double et triple tap
    }

    // Variables pour la détection du double / triple tap
    private var tapCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private var pendingDoubleTapRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_screen)
        gestureDetector = GestureDetectorCompat(this, this)
        gestureDetector.setOnDoubleTapListener(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) true else super.onTouchEvent(event)
    }

    // Détection du swipe pour accepter/rejeter via glissement (inchangé)
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Timber.d("Swipe right detected: appel accepté")
                    MyInCallService.answerCall()
                } else {
                    Timber.d("Swipe left detected: appel rejeté")
                    MyInCallService.rejectCall()
                }
                finish() // Ferme l'activité après l'action
                return true
            }
        }
        return false
    }

    // Méthodes de GestureDetector.OnGestureListener
    override fun onDown(e: MotionEvent?) = true
    override fun onShowPress(e: MotionEvent?) {}
    override fun onSingleTapUp(e: MotionEvent?) = true
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) = false
    override fun onLongPress(e: MotionEvent?) {}

    // Méthodes de GestureDetector.OnDoubleTapListener

    // Sur le premier double tap, on initialise le compteur et on planifie l'action d'acceptation
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        tapCount = 2
        // Planifie l'acceptation en cas de non-intervention d'un troisième tap dans le délai imparti
        pendingDoubleTapRunnable = Runnable {
            Timber.d("Double tap confirmé: appel accepté")
            MyInCallService.answerCall()
            finish()
            tapCount = 0
        }
        handler.postDelayed(pendingDoubleTapRunnable!!, TRIPLE_TAP_TIMEOUT)
        return true
    }

    // On surveille les événements de double tap pour détecter un troisième tap
    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        // On vérifie uniquement les ACTION_DOWN pour incrémenter le compteur
        if (e.action == MotionEvent.ACTION_DOWN) {
            // Si déjà à 2 taps et qu'un nouveau tap survient, c'est un triple tap
            if (tapCount == 2) {
                tapCount = 3
                // Annule l'action d'acceptation prévue pour le double tap
                pendingDoubleTapRunnable?.let { handler.removeCallbacks(it) }
                Timber.d("Triple tap détecté: appel rejeté")
                MyInCallService.rejectCall()
                finish()
                tapCount = 0
            }
        }
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        // On ne fait rien ici pour ne pas interférer avec la détection double/triple tap
        return true
    }
}
