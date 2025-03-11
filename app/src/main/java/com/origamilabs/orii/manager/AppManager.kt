package com.origamilabs.orii.manager

import android.content.Context
import androidx.room.Room
import com.origamilabs.orii.Constants
import com.origamilabs.orii.db.AppDatabase
import com.origamilabs.orii.db.SharedPreferences
import com.origamilabs.orii.models.FirmwareVersionInfo
import com.origamilabs.orii.models.User

/**
 * Singleton qui gère l'état global de l'application.
 */
object AppManager {
    var batteryLevel: Int = -1
        private set
    var firmwareVersion: Int = -1
        private set
    var firmwareVersionChecked: Boolean = false
        private set
    var canFirmwareUpdate: Boolean = false
        private set
    var canFirmwareForceUpdate: Boolean = false
        private set
    var firmwareVersionInfo: FirmwareVersionInfo? = null
        private set

    lateinit var database: AppDatabase
        private set
    lateinit var sharedPreferences: SharedPreferences
        private set

    fun init(applicationContext: Context) {
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "orii-app-db"
        ).build()

        sharedPreferences = SharedPreferences.INSTANCE.apply { init(applicationContext) }
        // Autres initialisations éventuelles…
    }

    fun close() { /* Implémentez la fermeture des ressources si nécessaire */ }

    fun getBatteryLevel() = batteryLevel
    fun setBatteryLevel(level: Int) {
        batteryLevel = level
    }

    fun getFirmwareVersion() = firmwareVersion
    fun setFirmwareVersion(version: Int) {
        firmwareVersion = version
    }

    fun getFirmwareVersionChecked() = firmwareVersionChecked
    fun setFirmwareVersionChecked(checked: Boolean) {
        firmwareVersionChecked = checked
    }

    fun getCanFirmwareUpdate() = canFirmwareUpdate
    fun setCanFirmwareUpdate(update: Boolean) {
        canFirmwareUpdate = update
    }

    fun getCanFirmwareForceUpdate() = canFirmwareForceUpdate
    fun setCanFirmwareForceUpdate(forceUpdate: Boolean) {
        canFirmwareForceUpdate = forceUpdate
    }

    fun getFirmwareVersionInfo() = firmwareVersionInfo
    fun setFirmwareVersionInfo(info: FirmwareVersionInfo) {
        firmwareVersionInfo = info
    }
}
