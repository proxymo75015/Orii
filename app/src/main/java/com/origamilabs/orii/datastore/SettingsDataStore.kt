package com.origamilabs.orii.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "com.origamilabs.orii.db"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

object SettingsDataStore {

    private val LAST_TAB_INDEX = intPreferencesKey("last_tab_index")

    fun getLastTabIndex(context: Context): Flow<Int> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[LAST_TAB_INDEX] ?: 0
        }
    }

    suspend fun setLastTabIndex(context: Context, index: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[LAST_TAB_INDEX] = index
        }
    }
}
