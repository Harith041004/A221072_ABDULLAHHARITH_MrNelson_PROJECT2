package com.example.vitalityapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class VitalityData(
    val movement: Int,
    val nutrition: Int,
    val sleep: Int,
    val mood: Int,
    val note: String,
    val goal: String,
    val isGoalSubmitted: Boolean,
    val isNoteSubmitted: Boolean
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vitality_prefs")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val MOVEMENT = intPreferencesKey("movement")
        val NUTRITION = intPreferencesKey("nutrition")
        val SLEEP = intPreferencesKey("sleep")
        val MOOD = intPreferencesKey("mood")
        val NOTE = stringPreferencesKey("note")
        val GOAL = stringPreferencesKey("goal")
        val IS_GOAL_SUBMITTED = booleanPreferencesKey("is_goal_submitted")
        val IS_NOTE_SUBMITTED = booleanPreferencesKey("is_note_submitted")
    }

    suspend fun saveSettings(data: VitalityData) {
        dataStore.edit { preferences ->
            preferences[MOVEMENT] = data.movement
            preferences[NUTRITION] = data.nutrition
            preferences[SLEEP] = data.sleep
            preferences[MOOD] = data.mood
            preferences[NOTE] = data.note
            preferences[GOAL] = data.goal
            preferences[IS_GOAL_SUBMITTED] = data.isGoalSubmitted
            preferences[IS_NOTE_SUBMITTED] = data.isNoteSubmitted
        }
    }

    val getSettings: Flow<VitalityData> = dataStore.data.map { preferences ->
        VitalityData(
            movement = preferences[MOVEMENT] ?: 15,
            nutrition = preferences[NUTRITION] ?: 20,
            sleep = preferences[SLEEP] ?: 12,
            mood = preferences[MOOD] ?: 18,
            note = preferences[NOTE] ?: "",
            goal = preferences[GOAL] ?: "",
            isGoalSubmitted = preferences[IS_GOAL_SUBMITTED] ?: false,
            isNoteSubmitted = preferences[IS_NOTE_SUBMITTED] ?: false
        )
    }
}
