package com.origamilabs.orii.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.origamilabs.orii.models.VoiceAssistantCounter

/**
 * Les seules entités conservées ici sont:
 * - CallLog (pour CallLogDao)
 * - VoiceAssistantCounter (pour VoiceAssistantCounterDao)
 */
@Database(
    entities = [
        CallLog::class,
        VoiceAssistantCounter::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun callLogDao(): CallLogDao
    abstract fun vaCounterDao(): VoiceAssistantCounterDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // On la rend 'private'
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS `va_counter` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `times` INTEGER NOT NULL,
                    `date` INTEGER NOT NULL
                )
                """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_app_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
