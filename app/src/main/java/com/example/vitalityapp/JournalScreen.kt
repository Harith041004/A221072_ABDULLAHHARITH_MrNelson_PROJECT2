package com.example.vitalityapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.*

@Composable
fun JournalScreen(dataStoreManager: DataStoreManager, viewModel: VitalityViewModel) {
    val currentNote by viewModel.journalNoteInput.collectAsStateWithLifecycle()
    val selectedMood by viewModel.journalMoodInput.collectAsStateWithLifecycle()
    val entries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val score by viewModel.dailyScore.collectAsStateWithLifecycle()
    
    val moods = listOf("😊 Happy", "😌 Calm", "😢 Sad", "😤 Stressed", "🥗 Good", "😴 Tired")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Journal", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Track your daily reflections", fontSize = 14.sp, color = TextSecondary)

        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    CircularProgressIndicator(
                        progress = { score / 100f },
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 4.dp
                    )
                    Text("$score", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Daily Vitality Score", color = Color.White, fontSize = 18.sp)
            }
        }

        Text("Habit Tracker", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        
        habits.forEach { habit ->
            HabitEntryCard(habit) { newValue -> viewModel.updateHabitValue(habit.id, newValue, dataStoreManager) }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Reflection", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Mood selector
                Text("How are you feeling?", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { viewModel.updateJournalMoodInput(mood) },
                            label = { Text(mood, fontSize = 12.sp) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = currentNote,
                    onValueChange = { viewModel.updateJournalNoteInput(it) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    label = { Text("How was your day?") },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Button(
                    onClick = { 
                        if (currentNote.isNotBlank()) {
                            viewModel.addJournalEntry(currentNote, selectedMood, dataStoreManager)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) { 
                    Text("Save Entry") 
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Past Entries", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (entries.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📝", fontSize = 32.sp)
                    Text("No entries yet", fontSize = 14.sp, color = TextSecondary)
                    Text("Write your first reflection above", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                entries.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(entry.date, fontSize = 12.sp, color = Color.Gray)
                                Text(entry.mood.ifBlank { "📝" }, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(entry.content, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HabitEntryCard(habit: Habit, onValueChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(habit.color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = habit.icon, contentDescription = null, tint = habit.color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.name, fontWeight = FontWeight.Bold)
                    Text("${habit.value}/${habit.goal} ${habit.unit}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Slider(
                value = habit.value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..habit.goal.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = habit.color,
                    activeTrackColor = habit.color,
                    inactiveTrackColor = habit.color.copy(alpha = 0.1f)
                )
            )
        }
    }
}
