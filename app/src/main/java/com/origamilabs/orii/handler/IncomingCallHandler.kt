package com.origamilabs.orii.handler

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.core.bluetooth.manager.CommandManager
import com.origamilabs.orii.notification.receivers.PhoneCallReceiver
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class IncomingCallHandler : PhoneCallReceiver.OnPhoneCallStateChangeListener {

    private var timer: Timer? = null

    override fun onIncomingCallReceived(ctx: Context, number: String, start: Date) {
        Log.d(TAG, "onIncomingCallReceived: $number")
        // Récupère le nom de la personne associée au numéro
        val personName = AppManager.INSTANCE.getPersonNameByNumber(number)
        timer = Timer()
        // Planifie une tâche répétée toutes les 3 secondes
        timer?.schedule(object : TimerTask() {
            override fun run() {
                AppManager.INSTANCE.runQueryOnBackground {
                    val app = AppManager.INSTANCE.getDatabase().applicationDao().findByPackageName("phonecall")
                    var person = AppManager.INSTANCE.getDatabase().personDao().findByPersonName(personName)
                    if (app != null) {
                        if (person == null) {
                            person = com.origamilabs.orii.models.Application(-1, 0, 0, "")
                        }
                        CommandManager.getInstance().putCallMessageReceivedTask(
                            app.ledColor,
                            app.vibration,
                            person.ledColor,
                            person.vibration,
                            false
                        )
                    }
                }
            }
        }, 0L, 3000L)
        // Arrête le timer après 180 secondes (3 minutes)
        Handler(Looper.getMainLooper()).postDelayed({
            stopTimer()
        }, 180000L)
    }

    override fun onIncomingCallAnswered(ctx: Context, number: String, start: Date) {
        Log.d(TAG, "onIncomingCallAnswered: $number")
        stopTimer()
    }

    override fun onIncomingCallEnded(ctx: Context, number: String, start: Date, end: Date) {
        Log.d(TAG, "onIncomingCallEnded: $number")
        stopTimer()
    }

    override fun onMissedCall(ctx: Context, number: String, start: Date) {
        Log.d(TAG, "onMissedCall: $number")
        stopTimer()
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    companion object {
        private const val TAG = "IncomingCallHandler"
    }
}
