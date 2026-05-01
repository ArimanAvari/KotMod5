package com.example.todolist.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.todoDataStore by preferencesDataStore(name = "todo_settings")

class TodoPreferences(
    private val context: Context
) {
    private val importDoneKey = booleanPreferencesKey("import_done")
    private val highlightCompletedKey = booleanPreferencesKey("highlight_completed")

    val highlightCompletedFlow: Flow<Boolean> = context.todoDataStore.data.map { prefs ->
        prefs[highlightCompletedKey] ?: false
    }

    suspend fun isImportDone(): Boolean {
        return context.todoDataStore.data.first()[importDoneKey] ?: false
    }

    suspend fun markImportDone() {
        context.todoDataStore.edit { prefs ->
            prefs[importDoneKey] = true
        }
    }

    suspend fun setHighlightCompleted(enabled: Boolean) {
        context.todoDataStore.edit { prefs ->
            prefs[highlightCompletedKey] = enabled
        }
    }
}
