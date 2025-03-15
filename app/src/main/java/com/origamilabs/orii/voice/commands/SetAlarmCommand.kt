package com.origamilabs.orii.voice.commands

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SetAlarmCommand @Inject constructor() : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Timber.d("Alarme déclenchée")
            Toast.makeText(it, "L'alarme se déclenche !", Toast.LENGTH_LONG).show()

            val tts = TextToSpeech(it) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts.language = Locale.FRANCE
                    tts.speak("L'alarme se déclenche !", TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
    }

    fun scheduleAlarm(context: Context, command: String, speak: (String) -> Unit) {
        val intent = Intent(context, SetAlarmCommand::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply { add(Calendar.SECOND, 30) } // Ex: 30 sec pour tester
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.FRANCE).format(calendar.time)
        Timber.d("Alarme réglée à $formattedTime")
        speak("L'alarme est réglée à $formattedTime")
    }

    fun cancelAlarm(context: Context, speak: (String) -> Unit) {
        val intent = Intent(context, SetAlarmCommand::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
        Timber.d("Alarme annulée")
        speak("L'alarme est annulée")
    }
}
