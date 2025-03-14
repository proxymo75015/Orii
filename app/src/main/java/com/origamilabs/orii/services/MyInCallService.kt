package com.origamilabs.orii.services

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.content.Intent
import com.origamilabs.orii.ui.CallScreenActivity
import timber.log.Timber
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class MyInCallService : InCallService() {

    companion object {
        // Stocke l'appel courant pour que l'activité puisse y accéder.
        var currentCall: Call? = null

        fun answerCall() {
            currentCall?.answer(0)
        }

        fun rejectCall() {
            currentCall?.disconnect()
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
        val callState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            call.details.state
        } else {
            @Suppress("DEPRECATION")
            call.state
        }
        Timber.d("Nouvel appel ajouté: $callState")

        if (callState == Call.STATE_RINGING) {
            // Lancer l'activité de gestion d'appel avec une interface utilisateur plus riche.
            val intent = Intent(this, CallScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (currentCall == call) {
            currentCall = null
        }
        Timber.d("Appel supprimé")
    }
}
