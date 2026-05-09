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
    val isNoteSubmitted: Boolean,
    val lastActivityDate: Long,
    val currentStreak: Int,
    val userName: String
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vitalityprefs")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val MOVEMENT = intPreferencesKey("movement")
        val NUTRITION = intPreferencesKey("nutrition")
        val SLEEP = intPreferencesKey("sleep")
        val MOOD = intPreferencesKey("mood")
        val NOTE = stringPreferencesKey("note")
        val GOAL = stringPreferencesKey("goal")
        val ISGOALSUBMITTED = booleanPreferencesKey("isgoalsubmitted")
        val ISNOTESUBMITTED = booleanPreferencesKey("isnotesubmitted")
        val LAST_ACTIVITY_DATE = longPreferencesKey("last_activity_date")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    suspend fun saveSettings(data: VitalityData) {
        dataStore.edit { preferences ->
            preferences[MOVEMENT] = data.movement
            preferences[NUTRITION] = data.nutrition
            preferences[SLEEP] = data.sleep
            preferences[MOOD] = data.mood
            preferences[NOTE] = data.note
            preferences[GOAL] = data.goal
            preferences[ISGOALSUBMITTED] = data.isGoalSubmitted
            preferences[ISNOTESUBMITTED] = data.isNoteSubmitted
            preferences[LAST_ACTIVITY_DATE] = data.lastActivityDate
            preferences[CURRENT_STREAK] = data.currentStreak
            preferences[USER_NAME] = data.userName
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
            isGoalSubmitted = preferences[ISGOALSUBMITTED] ?: false,
            isNoteSubmitted = preferences[ISNOTESUBMITTED] ?: false,
            lastActivityDate = preferences[LAST_ACTIVITY_DATE] ?: 0L,
            currentStreak = preferences[CURRENT_STREAK] ?: 0,
            userName = preferences[USER_NAME] ?: "User"
        )
    }
}
