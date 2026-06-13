package com.example.vitalityapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.*
import java.util.*

@Composable
fun ProfileScreen(dataStoreManager: DataStoreManager, viewModel: VitalityViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val healthGoals by viewModel.healthGoals.collectAsStateWithLifecycle()
    val dailyScore by viewModel.dailyScore.collectAsStateWithLifecycle()
    
    val isEditingName by viewModel.isEditingName.collectAsStateWithLifecycle()
    val editedName by viewModel.editedName.collectAsStateWithLifecycle()
    
    val completedGoalsCount = healthGoals.count { it.isAchieved }
    val totalGoalsCount = healthGoals.size

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).background(BackgroundLight)) {
        Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Header
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(SurfaceWhite), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).background(VitalityPurple, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = profile.name.take(1), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isEditingName) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { viewModel.updateEditedName(it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(onClick = {
                            viewModel.updateProfileName(editedName, dataStoreManager)
                            viewModel.setEditingName(false)
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                        IconButton(onClick = {
                            viewModel.setEditingName(false)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.setEditingName(true) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit name", modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                Text(text = "Member since Jan 2024", fontSize = 14.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(Icons.Default.LocalFireDepartment, profile.streak, "Streak", VitalityOrange)
                    StatItem(Icons.Default.BarChart, dailyScore.toString(), "Daily Score", VitalityBlue)
                    StatItem(Icons.Default.EmojiEvents, "$completedGoalsCount/$totalGoalsCount", "Goals", VitalityTeal)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Achievements", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Grid View
        getAchievements(healthGoals).chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { achievement -> AchievementGridItem(achievement) }
                if (row.size < 3) repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

data class Achievement(val icon: ImageVector, val title: String, val isUnlocked: Boolean)

@Composable
fun RowScope.AchievementGridItem(achievement: Achievement) {
    Card(
        modifier = Modifier.weight(1f).aspectRatio(1f), 
        colors = CardDefaults.cardColors(if (achievement.isUnlocked) SurfaceWhite else Color.Gray.copy(0.1f)),
        elevation = CardDefaults.cardElevation(if (achievement.isUnlocked) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                imageVector = if (achievement.isUnlocked) achievement.icon else Icons.Default.Lock, 
                contentDescription = achievement.title,
                tint = if (achievement.isUnlocked) VitalityPurple else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(achievement.title, fontSize = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

fun getAchievements(healthGoals: List<HealthGoal>): List<Achievement> {
    val goalAchievements = healthGoals.map { goal ->
        Achievement(
            icon = when (goal.category.lowercase()) {
                "movement" -> Icons.AutoMirrored.Filled.DirectionsRun
                "nutrition" -> Icons.Default.Restaurant
                "sleep" -> Icons.Default.Bedtime
                "mood" -> Icons.Default.SelfImprovement
                else -> Icons.Default.TrackChanges
            },
            title = goal.title.take(12),
            isUnlocked = goal.isAchieved
        )
    }
    
    return listOf(
        Achievement(Icons.Default.WbTwilight, "Early Bird", true),
        Achievement(Icons.Default.WaterDrop, "Hydration Pro", true),
        Achievement(Icons.Default.SelfImprovement, "Zen Master", true),
        Achievement(Icons.Default.LocalFireDepartment, "7 Day Streak", true),
        Achievement(Icons.Default.Restaurant, "Clean Eater", false),
        Achievement(Icons.Default.Bedtime, "Deep Sleeper", false)
    ) + goalAchievements.take(4)
}
