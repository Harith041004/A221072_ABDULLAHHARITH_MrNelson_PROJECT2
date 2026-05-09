package com.example.vitalityapp

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(dataStoreManager: DataStoreManager, viewModel: VitalityViewModel) {
    val savedData by dataStoreManager.getSettings.collectAsStateWithLifecycle(
        initialValue = VitalityData(15, 20, 12, 18, "", "", false, false, 0L, 0, "User")
    )
    
    // Hoisted state from ViewModel
    val healthGoalInput by viewModel.homeGoalInput.collectAsStateWithLifecycle()
    val showGoalDialog by viewModel.showHomeGoalDialog.collectAsStateWithLifecycle()
    val totalScore by viewModel.dailyScore.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    
    val todayDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(BackgroundLight)) {
        // HD Gradient Header
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(VitalityPurple, VitalityPurpleLight))).padding(24.dp)) {
            Column {
                Text("Good ${getGreeting()}! 👋", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text(todayDate, color = Color.White.copy(0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Today's Vitality", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$totalScore", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                            Text("/100", color = Color.White.copy(0.6f), fontSize = 24.sp, modifier = Modifier.padding(bottom = 12.dp))
                        }
                        LinearProgressIndicator(
                            progress = { totalScore / 100f },
                            modifier = Modifier.width(180.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(0.3f)
                        )
                    }
                    Text(text = if (totalScore >= 80) "🌟" else "💪", fontSize = 72.sp)
                }
            }
        }

        // Floating Goal Card
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp).clickable { 
            viewModel.updateHomeGoalInput(savedData.goal) // Pre-fill with current goal
            viewModel.setShowHomeGoalDialog(true)
        }, 
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardWhite), elevation = CardDefaults.cardElevation(4.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(VitalityOrange.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Text("🎯") }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (savedData.isGoalSubmitted) "Today's Goal" else "Set a Goal", fontSize = 12.sp, color = TextSecondary)
                    Text(if (savedData.isGoalSubmitted) savedData.goal else "Tap to add your focus", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Icon(if (savedData.isGoalSubmitted) Icons.Default.CheckCircle else Icons.Default.Add, null, tint = if (savedData.isGoalSubmitted) SuccessGreen else TextSecondary)
            }
        }

        Text("Daily Metrics", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        
        // Metrics from ViewModel habits
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            habits.forEach { habit ->
                MetricCard(habit.name, habit.emoji, habit.value, habit.goal, habit.color) { newValue ->
                    viewModel.updateHabitValue(habit.id, newValue, dataStoreManager)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowHomeGoalDialog(false) },
            title = { Text("Today's Focus") },
            text = { OutlinedTextField(value = healthGoalInput, onValueChange = { viewModel.updateHomeGoalInput(it) }, label = { Text("Enter goal") }) },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateHomeGoal(healthGoalInput, dataStoreManager)
                    viewModel.setShowHomeGoalDialog(false)
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowHomeGoalDialog(false) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MetricCard(title: String, emoji: String, score: Int, goal: Int, color: Color, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expanded = !expanded }
            .animateContentSize(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)),
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardWhite), elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(color))
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Text(emoji) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("$score/$goal", color = color, fontWeight = FontWeight.Bold)
                }
                if (expanded) {
                    Slider(
                        value = score.toFloat(), 
                        onValueChange = { onValueChange(it.toInt()) }, 
                        valueRange = 0f..goal.toFloat(),
                        colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
                    )
                }
            }
        }
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
}
