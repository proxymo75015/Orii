package com.origamilabs.orii.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

abstract class AppDatabase : RoomDatabase() {

    abstract fun applicationDao(): ApplicationDao
    abstract fun personDao(): PersonDao
    abstract fun vaCounterDao(): VoiceAssistantCounterDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `va_counter` (" +
                            "`date` INTEGER NOT NULL, " +
                            "`times` INTEGER NOT NULL, " +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
                            ")"
                )
            }
        }
    }
}
