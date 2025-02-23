package com.origamilabs.orii.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.origamilabs.orii.R
import com.origamilabs.orii.core.Constants
import com.origamilabs.orii.ui.main.MainActivity

/**
 * Service Firebase Messaging pour recevoir les notifications push.
 * Lors de la réception d'un message, ce service extrait le contenu de la notification,
 * crée une intent pour lancer MainActivity et affiche une notification dans la barre d'état.
 */
class OriiFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "OriiFirebaseService"
        private const val CHANNEL_ID = "orii_notification_channel"

        /**
         * Récupère et affiche le token Firebase pour le débogage.
         */
        fun fetchFirebaseToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Erreur lors de la récupération du token FCM", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d(TAG, "Token FCM: $token")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nouveau token reçu: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message reçu de: ${remoteMessage.from}")

        remoteMessage.notification?.let { notification ->
            val body = notification.body ?: return@let
            Log.d(TAG, "Notification: ${notification.title} - $body")
            sendNotification(notification.title, body)
        }
    }

    /**
     * Envoie le token FCM au serveur pour permettre l'envoi de notifications ciblées.
     */
    private fun sendTokenToServer(token: String) {
        Log.d(TAG, "Envoi du token au serveur: $token")
        // Implémentez ici l'envoi du token à votre backend.
    }

    /**
     * Affiche une notification sur l'appareil.
     */
    private fun sendNotification(title: String?, messageBody: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Création du canal de notification pour Android Oreo et versions ultérieures.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.notification_push_channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent pour ouvrir MainActivity lors du clic sur la notification.
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Définition des flags adaptés aux versions d'Android.
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            Constants.ORII_NOTIFICATION_PUSH,
            intent,
            pendingIntentFlags
        )

        // Construction de la notification.
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_statusbar)
            .setContentTitle(title ?: getString(R.string.app_name))
            .setContentText(messageBody ?: "Vous avez reçu une nouvelle notification")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Affichage de la notification.
        notificationManager.notify(Constants.ORII_NOTIFICATION_PUSH, notificationBuilder.build())
    }
}
