package com.origamilabs.orii.voice.commands

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GetTimeCommand {
    fun execute(context: Context, speak: (String) -> Unit) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        speak("Il est $currentTime")
    }
}
