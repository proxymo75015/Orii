package com.origamilabs.orii.generated.callback

import android.view.View

class OnClickListener(
    private val mListener: Listener,
    private val mSourceId: Int
) : View.OnClickListener {

    interface Listener {
        fun _internalCallbackOnClick(sourceId: Int, view: View)
    }

    override fun onClick(view: View) {
        mListener._internalCallbackOnClick(mSourceId, view)
    }
}
