package com.origamilabs.orii.utils

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import com.facebook.appevents.AppEventsConstants
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.models.CalendarEvent
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar

object CalendarContentResolver {

    private val PROJECTION = arrayOf("title", "begin", AnalyticsManager.ActionState.END, "allDay")
    private const val PROJECTION_TITLE_INDEX = 0
    private const val PROJECTION_BEGIN_TIME_INDEX = 1
    private const val PROJECTION_END_TIME_INDEX = 2
    private const val PROJECTION_ALL_DAY_INDEX = 3
    private const val TAG = "CalendarContentResolver"

    /**
     * Récupère les événements du jour sous forme d'une liste de [CalendarEvent].
     *
     * @param context le contexte utilisé pour accéder au ContentResolver
     * @return une liste d'événements pour aujourd'hui
     */
    fun getTodayEvent(context: Context): ArrayList<CalendarEvent> {
        val contentResolver = context.contentResolver

        // Définition de l'intervalle de temps pour aujourd'hui
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val startMillis = calendar.timeInMillis
        calendar.set(year, month, day, 23, 59, 59)
        val endMillis = calendar.timeInMillis

        // Construction de l'URI pour interroger les instances d'événements du calendrier
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, startMillis)
            ContentUris.appendId(this, endMillis)
        }.build()

        // Formatage de l'heure en fonction de la locale de l'appareil (DeviceLocale est supposé être défini)
        val simpleDateFormat = SimpleDateFormat("hh:mm a", DeviceLocale.instance.deviceLocale)

        val events = ArrayList<CalendarEvent>()
        // Exécution de la requête dans un bloc "use" afin de fermer automatiquement le curseur
        contentResolver.query(uri, PROJECTION, null, null, "startDay ASC, startMinute ASC")?.use { cursor ->
            while (cursor.moveToNext()) {
                val title = cursor.getString(PROJECTION_TITLE_INDEX)
                val beginTime = simpleDateFormat.format(cursor.getLong(PROJECTION_BEGIN_TIME_INDEX))
                val endTime = simpleDateFormat.format(cursor.getLong(PROJECTION_END_TIME_INDEX))
                // Vérifie si l'événement est marqué comme "all day" (la valeur "0" correspond à non)
                val allDay = cursor.getString(PROJECTION_ALL_DAY_INDEX) != AppEventsConstants.EVENT_PARAM_VALUE_NO
                events.add(CalendarEvent(title, beginTime, endTime, allDay))
            }
        }
        return events
    }
}
