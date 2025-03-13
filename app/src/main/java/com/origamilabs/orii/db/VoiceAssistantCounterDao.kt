package com.origamilabs.orii.db

import androidx.room.*
import com.origamilabs.orii.models.VoiceAssistantCounter
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceAssistantCounterDao {

    // Lecture continue en Flow : Room émettra automatiquement de nouvelles listes en cas de changement
    @Query("SELECT * FROM va_counter")
    fun getAllAsFlow(): Flow<List<VoiceAssistantCounter>>

    // Lecture ponctuelle en mode suspend (si vous préférez un simple appel)
    @Query("SELECT * FROM va_counter")
    suspend fun getAllOnce(): List<VoiceAssistantCounter>

    // Insertion
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg counters: VoiceAssistantCounter)

    // Mise à jour
    @Update
    suspend fun update(vararg counters: VoiceAssistantCounter)

    // Suppression
    @Delete
    suspend fun delete(counter: VoiceAssistantCounter)

    // Tout effacer
    @Query("DELETE FROM va_counter")
    suspend fun clear()
}
