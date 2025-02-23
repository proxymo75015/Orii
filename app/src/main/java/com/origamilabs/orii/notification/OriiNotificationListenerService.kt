package com.origamilabs.orii.notification

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.origamilabs.orii.Constants
import com.origamilabs.orii.notification.NotificationManager
import com.origamilabs.orii.notification.processor.Processor
import com.origamilabs.orii.notification.processor.line.LineProcessor
import com.origamilabs.orii.notification.processor.messenger.MessengerProcessor
import com.origamilabs.orii.notification.processor.wechat.WeChatProcessor
import com.origamilabs.orii.notification.processor.whatsapp.ChineseProcessor
import com.origamilabs.orii.notification.processor.whatsapp.DeutschProcessor
import com.origamilabs.orii.notification.processor.whatsapp.EnglishProcessor
import com.origamilabs.orii.notification.processor.whatsapp.FrenchProcessor
import com.origamilabs.orii.notification.processor.whatsapp.ItalianoProcessor
import com.origamilabs.orii.notification.processor.whatsapp.JapaneseProcessor
import com.origamilabs.orii.notification.processor.whatsapp.SpanishProcessor
import java.util.Locale
import kotlin.text.Regex

/**
 * Service d'écoute des notifications, permettant de traiter les notifications
 * provenant d'applications supportées et d'envoyer des broadcasts en fonction.
 */
class OriiNotificationListenerService : NotificationListenerService() {

    private val TAG = "NLS"

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        NotificationManager.init()
        NotificationManager.start()
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "onNotificationPosted")

        val packageName = sbn?.packageName
        Log.d(TAG, "Package: ${packageName ?: "null"}")

        val locale = Locale.getDefault()
        Log.d(TAG, "Current Language: ${locale.displayLanguage}:${locale.language}")

        if (isSupportedApps(packageName)) {
            // Récupération du tickerText s'il existe
            val tickerText = sbn?.notification?.tickerText?.toString()

            // Récupération des données de la notification via le bundle
            val extras: Bundle = sbn?.notification?.extras
                ?: run {
                    Log.e(TAG, "Notification extras is null")
                    return
                }
            val title: String? = extras.getString(NotificationCompat.EXTRA_TITLE)
            val text: String? = extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()
            val textLines: Array<CharSequence>? = extras.getCharSequenceArray(NotificationCompat.EXTRA_TEXT_LINES)

            // Concatène les lignes de texte après suppression des espaces superflus
            val textLinesStr = textLines
                ?.mapNotNull { removeSpaces(it)?.toString() }
                ?.filter { it.isNotBlank() }
                ?.joinToString(separator = "\n")
                ?: ""

            Log.d(TAG, "----------Start of notification----------")
            Log.d(TAG, "Ticker Text: $tickerText")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")
            Log.d(TAG, "Text lines: $textLinesStr")
            Log.d(TAG, "----------End of notification----------")

            // Variables pour le traitement de la notification
            var sender: String? = null
            var message: String? = null
            va
