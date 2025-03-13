package com.origamilabs.orii.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.origamilabs.orii.utils.ResourceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire de notifications modernisé.
 * Utilise Hilt pour l'injection du contexte applicatif et du ResourceProvider.
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resourceProvider: ResourceProvider
) {
    companion object {
        private const val CHANNEL_ID = "default_channel"
        private const val CHANNEL_NAME = "Notifications Générales"
        private const val CHANNEL_DESCRIPTION = "Notifications de l'application"
        private var notificationId = 0
    }

    /**
     * Initialise le gestionnaire en créant le canal de notification pour les API 26+.
     */
    fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            systemNotificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Affiche une notification simple avec un titre et un message.
     *
     * Vérifie la permission POST_NOTIFICATIONS pour les API 33+ avant d'appeler notify.
     *
     * @param title Le titre de la notification.
     * @param message Le contenu textuel de la notification.
     */
    fun sendNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(resourceProvider.notificationIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    NotificationManagerCompat.from(context).notify(notificationId++, notification)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    // Gestion supplémentaire de l'erreur si nécessaire
                }
            } else {
                // La permission POST_NOTIFICATIONS n'est pas accordée.
                // Vous pouvez choisir de demander la permission ici ou de loguer une erreur.
            }
        } else {
            try {
                NotificationManagerCompat.from(context).notify(notificationId++, notification)
            } catch (e: SecurityException) {
                e.printStackTrace()
                // Gestion supplémentaire de l'erreur si nécessaire
            }
        }
    }

    /**
     * Annule toutes les notifications actives.
     */
    fun close() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
