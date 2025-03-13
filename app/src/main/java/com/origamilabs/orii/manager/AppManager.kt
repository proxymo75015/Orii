package com.origamilabs.orii.manager

import android.content.Context
import androidx.room.Room
import com.origamilabs.orii.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Singleton qui gère l'état global de l'application.
 */
object AppManager {

    // Scope pour exécuter du code en arrière-plan (IO) via coroutines
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Champs existants
    var batteryLevel: Int = -1
        private set
    var firmwareVersion: Int = -1
        private set

    // Accès à la base de données Room
    lateinit var database: AppDatabase
        private set

    /**
     * Initialise l'AppManager.
     * À appeler depuis l'Application ou un endroit central au démarrage.
     */
    fun init(applicationContext: Context) {
        // Initialisation DB Room
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "orii-app-db"
        ).build()

        // Si vous souhaitez stocker/restituer certaines valeurs via DataStore,
        // vous pouvez désormais l'appeler directement dans votre code,
        // par ex. SettingsDataStore.getMicMode(context) etc.
        // Aucune initialisation globale n'est requise pour DataStore.
    }

    /**
     * Permet d'exécuter une opération sur un thread IO via coroutines,
     * sans bloquer le thread principal.
     */
    fun runQueryOnBackground(block: suspend () -> Unit) {
        appScope.launch {
            block()
        }
    }

    /**
     * Libère éventuellement les ressources liées à AppManager.
     */
    fun close() {
        // Annule toutes les coroutines lancées via appScope
        appScope.cancel()
    }

    fun getBatteryLevel() = batteryLevel
    fun setBatteryLevel(level: Int) {
        batteryLevel = level
    }

    fun getFirmwareVersion() = firmwareVersion
    fun setFirmwareVersion(version: Int) {
        firmwareVersion = version
    }
}
