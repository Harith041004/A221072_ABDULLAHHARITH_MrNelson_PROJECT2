package com.example.vitalityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.VitalityAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStoreManager = DataStoreManager(this)

        enableEdgeToEdge()
        setContent {
            VitalityAppTheme {
                VitalityApp(dataStoreManager)
            }
        }
    }
}

// ============================================
// DATA CLASSES
// ============================================

data class MetricData(
    val id: String,
    val title: String,
    val emoji: String,
    val score: Int,
    val maxScore: Int = 25,
    val color: Color
)

// ============================================
// MAIN APP NAVIGATION
// ============================================
@Composable
fun VitalityApp(dataStoreManager: DataStoreManager) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceLight,
        bottomBar = {
            VitalityBottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(dataStoreManager)
                1 -> ScreenPlaceholder("Insights Screen")
                2 -> ScreenPlaceholder("Library Screen")
                3 -> ScreenPlaceholder("Journal Screen")
                4 -> ScreenPlaceholder("Profile Screen")
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun VitalityBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
        Triple("Insights", Icons.Filled.Insights, Icons.Outlined.Insights),
        Triple("Library", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        Triple("Journal", Icons.Filled.EditNote, Icons.Outlined.EditNote),
        Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar(containerColor = CardWhite, tonalElevation = 8.dp) {
        tabs.forEachIndexed { index, (label, filled, outlined) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(if (selectedTab == index) filled else outlined, label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VitalityPurple,
                    indicatorColor = VitalityPurple.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// ============================================
// HOME SCREEN
// ============================================
@Composable
fun HomeScreen(dataStoreManager: DataStoreManager) {
    val scope = rememberCoroutineScope()
    val savedData by dataStoreManager.getSettings.collectAsStateWithLifecycle(
        initialValue = VitalityData(15, 20, 12, 18, "", "", false, false)
    )

    var healthGoal by remember { mutableStateOf("") }
    var showGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(savedData.goal) {
        healthGoal = savedData.goal
    }

    val metrics = listOf(
        MetricData("movement", "Movement", "🏃", savedData.movement, 25, VitalityBlue),
        MetricData("nutrition", "Nutrition", "🥗", savedData.nutrition, 25, VitalityTeal),
        MetricData("sleep", "Sleep", "😴", savedData.sleep, 25, VitalityPurple),
        MetricData("mood", "Mood", "🧘", savedData.mood, 25, VitalityPink)
    )

    val totalScore = savedData.movement + savedData.nutrition + savedData.sleep + savedData.mood
    val todayDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
                        LinearProgressIndicator(progress = { totalScore / 100f }, modifier = Modifier.width(180.dp).height(8.dp).clip(RoundedCornerShape(4.dp)), color = Color.White, trackColor = Color.White.copy(0.3f))
                    }
                    Text(text = if (totalScore >= 80) "🌟" else if (totalScore >= 50) "😊" else "💪", fontSize = 72.sp)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp).clickable { showGoalDialog = true }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardWhite), elevation = CardDefaults.cardElevation(4.dp)) {
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
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            metrics.forEach { metric ->
                MetricCard(metric) { newScore ->
                    scope.launch {
                        val newData = when (metric.id) {
                            "movement" -> savedData.copy(movement = newScore)
                            "nutrition" -> savedData.copy(nutrition = newScore)
                            "sleep" -> savedData.copy(sleep = newScore)
                            "mood" -> savedData.copy(mood = newScore)
                            else -> savedData
                        }
                        dataStoreManager.saveSettings(newData)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Quick Tips", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(getTips()) { tip -> TipCard(tip) }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Today's Focus") },
            text = { OutlinedTextField(value = healthGoal, onValueChange = { healthGoal = it }, label = { Text("Enter goal") }) },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        dataStoreManager.saveSettings(savedData.copy(goal = healthGoal, isGoalSubmitted = true))
                        showGoalDialog = false
                    }
                }) { Text("Save") }
            }
        )
    }
}

@Composable
fun MetricCard(metric: MetricData, onScoreChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left Accent Color
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(metric.color)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(metric.color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(metric.emoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(metric.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1D1B20))
                        LinearProgressIndicator(
                            progress = { metric.score / 25f },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(CircleShape),
                            color = metric.color,
                            trackColor = metric.color.copy(alpha = 0.1f)
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Daily goal for ${metric.title}: 25 points. Current progress is ${(metric.score / 25f * 100).toInt()}%.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (metric.score > 0) onScoreChange(metric.score - 1) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = metric.color.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Remove, null, tint = metric.color)
                        }
                        Text(
                            "${metric.score}",
                            fontWeight = FontWeight.Bold,
                            color = metric.color,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 18.sp
                        )
                        IconButton(
                            onClick = { if (metric.score < 25) onScoreChange(metric.score + 1) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = metric.color.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Add, null, tint = metric.color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TipCard(tip: String) {
    Card(
        modifier = Modifier.width(200.dp).height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(VitalityPurple.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(tip, textAlign = TextAlign.Center, fontSize = 14.sp)
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

fun getTips(): List<String> = listOf(
    "Drink 8 glasses of water today",
    "Take a 10-minute walk",
    "Practice deep breathing",
    "Eat a serving of greens"
)

val SurfaceLight = Color(0xFFF8F9FA)
val CardWhite = Color(0xFFFFFFFF)
val VitalityPurple = Color(0xFF6750A4)
val VitalityPurpleLight = Color(0xFFD0BCFF)
val VitalityOrange = Color(0xFFFFB74D)
val TextSecondary = Color(0xFF757575)
val SuccessGreen = Color(0xFF4CAF50)
val VitalityBlue = Color(0xFF2196F3)
val VitalityTeal = Color(0xFF009688)
val VitalityPink = Color(0xFFE91E63)
