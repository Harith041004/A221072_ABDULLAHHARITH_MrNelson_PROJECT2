package com.example.vitalityapp

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

data class ProfileState(val name: String = "Abdullah Harith", val matric: String = "A221072", val streak: String = "15", val avgScore: String = "72")
data class JournalEntry(val date: String, val content: String, val mood: String)
data class Habit(val id: String, val name: String, val emoji: String, val value: Int, val goal: Int, val unit: String, val color: Color)

class VitalityViewModel : ViewModel() {
    private val _profile = MutableStateFlow(ProfileState())
    val profile = _profile.asStateFlow()

    private val _journalEntries = MutableStateFlow(listOf(JournalEntry("Oct 24, 2024", "Started my journey!", "😊")))
    val journalEntries = _journalEntries.asStateFlow()

    private val _habits = MutableStateFlow(listOf(
        Habit("move", "Steps", "🏃", 15, 25, "k", Color(0xFF2196F3)),
        Habit("nutri", "Water", "🥗", 20, 25, "gls", Color(0xFF009688))
    ))
    val habits = _habits.asStateFlow()

    private val _dailyScore = MutableStateFlow(72)
    val dailyScore = _dailyScore.asStateFlow()

    fun updateHabitValue(id: String, newValue: Int) {
        _habits.update { current -> current.map { if (it.id == id) it.copy(value = newValue) else it } }
        _dailyScore.value = (_habits.value.sumOf { (it.value.toFloat() / it.goal * 25).toInt() }).coerceIn(0, 100)
    }

    fun addJournalEntry(content: String, mood: String) {
        val date = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        _journalEntries.update { listOf(JournalEntry(date, content, mood)) + it }
    }
}