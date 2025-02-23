package com.origamilabs.orii.ui.tutorial.phase.zero

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import kotlin.TypeCastException

class PairLaterFragment : TutorialFragment() {

    companion object {
        private const val TAG = "PairLaterFragment"
        @JvmStatic
        fun newInstance(): PairLaterFragment = PairLaterFragment()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            startCountDown()
        }
    }

    private fun startCountDown() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                val act = activity ?: throw TypeCastException("null cannot be cast to non-null type TutorialActivity")
                (act as TutorialActivity).exitTutorial()
            }
        }, 2000L)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_pair_later, container, false)
}
