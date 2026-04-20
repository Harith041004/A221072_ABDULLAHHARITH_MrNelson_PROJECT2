package com.example.vitalityapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

data class ProfileState(
    val name: String = "Abdullah Harith",
    val matric: String = "A221072",
    val status: String = "Software Engineering Student",
    val streak: String = "15",
    val avgScore: String = "72"
)

data class JournalEntry(
    val date: String,
    val content: String,
    val mood: String
)

data class Habit(
    val id: String,
    val name: String,
    val emoji: String,
    var value: Int,
    val goal: Int,
    val unit: String,
    val color: androidx.compose.ui.graphics.Color
)

class VitalityViewModel : ViewModel() {
    private val _profile = MutableStateFlow(ProfileState())
    val profile: StateFlow<ProfileState> = _profile.asStateFlow()

    private val _journalEntries = MutableStateFlow(listOf(
        JournalEntry("Oct 24, 2024", "Started my vitality journey today!", "😊"),
        JournalEntry("Oct 23, 2024", "Feeling great after a long walk.", "🌟")
    ))
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    private val _habits = MutableStateFlow(getDefaultHabits())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _dailyScore = MutableStateFlow(72) // Initial calculated score
    val dailyScore: StateFlow<Int> = _dailyScore.asStateFlow()

    fun addJournalEntry(content: String, mood: String) {
        val date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        val newEntry = JournalEntry(date, content, mood)
        _journalEntries.update { listOf(newEntry) + it }
    }

    fun updateHabitValue(id: String, newValue: Int) {
        _habits.update { currentHabits ->
            currentHabits.map { habit ->
                if (habit.id == id) habit.copy(value = newValue) else habit
            }
        }
        calculateScore()
    }

    private fun calculateScore() {
        val habits = _habits.value
        val total = habits.sumOf { (it.value.toFloat() / it.goal.toFloat() * 25).toInt() }
        _dailyScore.value = total.coerceIn(0, 100)
    }
}

fun getDefaultHabits(): List<Habit> = listOf(
    Habit("movement", "Steps", "🏃", 15, 25, "k steps", androidx.compose.ui.graphics.Color(0xFF2196F3)),
    Habit("nutrition", "Water", "🥗", 20, 25, "glasses", androidx.compose.ui.graphics.Color(0xFF009688)),
    Habit("sleep", "Sleep", "😴", 12, 25, "hours", androidx.compose.ui.graphics.Color(0xFF6750A4)),
    Habit("mood", "Meditation", "🧘", 18, 25, "mins", androidx.compose.ui.graphics.Color(0xFFE91E63))
)