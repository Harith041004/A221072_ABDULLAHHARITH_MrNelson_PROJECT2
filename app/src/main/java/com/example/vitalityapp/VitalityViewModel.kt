package com.example.vitalityapp

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data Models
data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val mood: String,
    val date: String = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
)

data class Habit(
    val id: String,
    val name: String,
    val emoji: String,
    val value: Int,
    val goal: Int,
    val unit: String,
    val color: Color
)

data class HealthGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val deadline: String,
    val motivationalNote: String = "",
    val isAchieved: Boolean = false,
    val createdAt: String = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())
)

data class UserProfile(
    val name: String = "User",
    val streak: String = "0",
    val avgScore: String = "0"
)

class VitalityViewModel : ViewModel() {
    
    // --- Persistent App State ---
    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()
    
    private val _habits = MutableStateFlow(getDefaultHabits())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()
    
    private val _healthGoals = MutableStateFlow<List<HealthGoal>>(emptyList())
    val healthGoals: StateFlow<List<HealthGoal>> = _healthGoals.asStateFlow()
    
    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()
    
    private val _dailyScore = MutableStateFlow(0)
    val dailyScore: StateFlow<Int> = _dailyScore.asStateFlow()

    // --- Hoisted UI States (Configuration Change Resilience) ---

    // Home Screen
    private val _showHomeGoalDialog = MutableStateFlow(false)
    val showHomeGoalDialog = _showHomeGoalDialog.asStateFlow()
    private val _homeGoalInput = MutableStateFlow("")
    val homeGoalInput = _homeGoalInput.asStateFlow()

    // Journal Screen
    private val _journalNoteInput = MutableStateFlow("")
    val journalNoteInput = _journalNoteInput.asStateFlow()
    private val _journalMoodInput = MutableStateFlow("")
    val journalMoodInput = _journalMoodInput.asStateFlow()

    // Goals Screen
    private val _showAddGoalDialog = MutableStateFlow(false)
    val showAddGoalDialog = _showAddGoalDialog.asStateFlow()
    private val _newGoalTitleInput = MutableStateFlow("")
    val newGoalTitleInput = _newGoalTitleInput.asStateFlow()
    private val _newGoalCategoryInput = MutableStateFlow("Movement")
    val newGoalCategoryInput = _newGoalCategoryInput.asStateFlow()
    private val _newGoalTargetInput = MutableStateFlow("")
    val newGoalTargetInput = _newGoalTargetInput.asStateFlow()
    private val _newGoalDeadlineInput = MutableStateFlow("")
    val newGoalDeadlineInput = _newGoalDeadlineInput.asStateFlow()
    private val _newGoalNoteInput = MutableStateFlow("")
    val newGoalNoteInput = _newGoalNoteInput.asStateFlow()

    // Profile Screen
    private val _isEditingName = MutableStateFlow(false)
    val isEditingName = _isEditingName.asStateFlow()
    private val _editedName = MutableStateFlow("")
    val editedName = _editedName.asStateFlow()

    private var currentData: VitalityData? = null
    private var isInitialized = false

    init {
        loadSampleData()
        updateDailyScore()
    }

    private fun getDefaultHabits() = listOf(
        Habit("movement", "Movement", "🏃", 15, 25, "pts", Color(0xFF2196F3)),
        Habit("nutrition", "Nutrition", "🥗", 20, 25, "pts", Color(0xFF009688)),
        Habit("sleep", "Sleep", "😴", 12, 25, "pts", Color(0xFF6750A4)),
        Habit("mood", "Mood", "🧘", 18, 25, "pts", Color(0xFFE91E63))
    )

    private fun loadSampleData() {
        _journalEntries.value = listOf(JournalEntry(content = "Started my vitality journey!", mood = "😊 Happy"))
        _healthGoals.value = listOf(
            HealthGoal(title = "Initial Health Goal", category = "Movement", targetValue = 25, currentValue = 15, deadline = "2024-12-31")
        )
    }

    // --- Synchronization & Streak Logic ---

    fun initializeFromDataStore(data: VitalityData, dataStoreManager: DataStoreManager) {
        currentData = data
        if (!isInitialized) {
            isInitialized = true
            _habits.value = listOf(
                Habit("movement", "Movement", "🏃", data.movement, 25, "pts", Color(0xFF2196F3)),
                Habit("nutrition", "Nutrition", "🥗", data.nutrition, 25, "pts", Color(0xFF009688)),
                Habit("sleep", "Sleep", "😴", data.sleep, 25, "pts", Color(0xFF6750A4)),
                Habit("mood", "Mood", "🧘", data.mood, 25, "pts", Color(0xFFE91E63))
            )
            updateDailyScore()
            updateRelatedGoals()
            updateStreakLogic(dataStoreManager)
        }
        _profile.value = _profile.value.copy(streak = data.currentStreak.toString(), name = data.userName)
    }

    fun updateStreakLogic(dataStoreManager: DataStoreManager) {
        val data = currentData ?: return
        val today = getMidnightTimestamp(0)
        val yesterday = getMidnightTimestamp(-1)

        if (data.lastActivityDate != 0L) {
            if (data.lastActivityDate < yesterday) {
                // Streak died
                persistStreak(0, data.lastActivityDate, dataStoreManager)
            } else if (data.lastActivityDate == yesterday) {
                // New day opening!
                persistStreak(data.currentStreak + 1, today, dataStoreManager)
            }
        } else {
            // First time ever
            persistStreak(1, today, dataStoreManager)
        }
    }

    private fun persistStreak(streak: Int, date: Long, dataStoreManager: DataStoreManager) {
        val data = currentData ?: return
        _profile.value = _profile.value.copy(streak = streak.toString())
        viewModelScope.launch {
            val updated = data.copy(currentStreak = streak, lastActivityDate = date)
            currentData = updated
            dataStoreManager.saveSettings(updated)
        }
    }

    fun onActivityLogged(dataStoreManager: DataStoreManager) {
        val data = currentData ?: return
        val today = getMidnightTimestamp(0)
        if (data.lastActivityDate < today) {
            updateStreakLogic(dataStoreManager)
        }
    }

    private fun getMidnightTimestamp(offset: Int) = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // --- State Setters ---
    fun setShowHomeGoalDialog(show: Boolean) { _showHomeGoalDialog.value = show }
    fun updateHomeGoalInput(input: String) { _homeGoalInput.value = input }
    fun updateJournalNoteInput(input: String) { _journalNoteInput.value = input }
    fun updateJournalMoodInput(mood: String) { _journalMoodInput.value = mood }
    fun setShowAddGoalDialog(show: Boolean) { _showAddGoalDialog.value = show }
    fun updateNewGoalTitle(v: String) { _newGoalTitleInput.value = v }
    fun updateNewGoalCategory(v: String) { _newGoalCategoryInput.value = v }
    fun updateNewGoalTarget(v: String) { _newGoalTargetInput.value = v }
    fun updateNewGoalDeadline(input: String) { _newGoalDeadlineInput.value = input }
    fun updateNewGoalNote(input: String) { _newGoalNoteInput.value = input }
    fun setEditingName(v: Boolean) { _isEditingName.value = v; if (v) _editedName.value = _profile.value.name }
    fun updateEditedName(v: String) { _editedName.value = v }

    // --- Actions ---
    fun addJournalEntry(content: String, mood: String, dataStoreManager: DataStoreManager) {
        if (content.isNotBlank()) {
            _journalEntries.value = listOf(JournalEntry(content = content, mood = mood.ifBlank { "📝" })) + _journalEntries.value
            _journalNoteInput.value = ""; _journalMoodInput.value = ""
            onActivityLogged(dataStoreManager)
        }
    }

    fun updateHabitValue(habitId: String, newValue: Int, dataStoreManager: DataStoreManager) {
        _habits.value = _habits.value.map { if (it.id == habitId) it.copy(value = newValue.coerceIn(0, it.goal)) else it }
        updateDailyScore(); updateRelatedGoals()
        
        viewModelScope.launch {
            val data = currentData ?: return@launch
            val updated = when(habitId) {
                "movement" -> data.copy(movement = newValue)
                "nutrition" -> data.copy(nutrition = newValue)
                "sleep" -> data.copy(sleep = newValue)
                "mood" -> data.copy(mood = newValue)
                else -> data
            }
            currentData = updated
            dataStoreManager.saveSettings(updated)
            onActivityLogged(dataStoreManager)
        }
    }

    fun updateHomeGoal(newGoal: String, dataStoreManager: DataStoreManager) {
        viewModelScope.launch {
            val data = currentData ?: return@launch
            val updated = data.copy(goal = newGoal, isGoalSubmitted = true)
            currentData = updated
            dataStoreManager.saveSettings(updated)
        }
    }

    fun updateProfileName(name: String, dataStoreManager: DataStoreManager) {
        _profile.value = _profile.value.copy(name = name)
        viewModelScope.launch {
            val data = currentData ?: return@launch
            val updated = data.copy(userName = name)
            currentData = updated
            dataStoreManager.saveSettings(updated)
        }
    }

    fun addHealthGoal(goal: HealthGoal) { _healthGoals.value += goal; clearAddGoalInputs() }
    private fun clearAddGoalInputs() {
        _newGoalTitleInput.value = ""; _newGoalCategoryInput.value = "Movement"
        _newGoalTargetInput.value = ""; _newGoalDeadlineInput.value = ""; _newGoalNoteInput.value = ""
    }
    fun updateHealthGoal(id: String, v: Int) { _healthGoals.value = _healthGoals.value.map { if (it.id == id) it.copy(currentValue = v, isAchieved = v >= it.targetValue) else it } }
    
    private fun updateDailyScore() { _dailyScore.value = _habits.value.sumOf { it.value }.coerceIn(0, 100) }
    private fun updateRelatedGoals() {
        _healthGoals.value = _healthGoals.value.map { goal ->
            val habit = _habits.value.find { it.id.lowercase() == goal.category.lowercase() }
            if (habit != null) goal.copy(currentValue = habit.value, isAchieved = habit.value >= goal.targetValue) else goal
        }
    }
}
