package com.example.vitalityapp

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.*

data class Achievement(val emoji: String, val title: String, val isUnlocked: Boolean)

@Composable
fun ProfileScreen(viewModel: VitalityViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Header
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(CardWhite), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).background(VitalityPurple, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = profile.name.take(1), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Member since Jan 2024", fontSize = 14.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("🔥", profile.streak, "Day Streak")
                    StatItem("📊", profile.avgScore, "Avg Score")
                    StatItem("🏆", "8", "Badges")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Achievements", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Grid View
        getAchievements().chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { achievement -> AchievementGridItem(achievement) }
                if (row.size < 3) repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardWhite)) {
            Column {
                SettingsItem("🔔", "Notifications", "Reminders and alerts")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem("🎨", "Appearance", "Theme and display")
            }
        }
    }
}

@Composable
fun RowScope.AchievementGridItem(achievement: Achievement) {
    Card(modifier = Modifier.weight(1f).aspectRatio(1f), colors = CardDefaults.cardColors(if (achievement.isUnlocked) CardWhite else Color.Gray.copy(0.1f))) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(if (achievement.isUnlocked) achievement.emoji else "🔒", fontSize = 28.sp)
            Text(achievement.title, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun SettingsItem(emoji: String, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(Color.Gray.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Text(emoji)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
    }
}

fun getAchievements(): List<Achievement> = listOf(
    Achievement("🏆", "Early Bird", true),
    Achievement("💧", "Hydration", true),
    Achievement("🧘", "Zen Master", true),
    Achievement("🔥", "7 Day Streak", true),
    Achievement("🥗", "Clean Eater", false),
    Achievement("😴", "Deep Sleeper", false)
)
