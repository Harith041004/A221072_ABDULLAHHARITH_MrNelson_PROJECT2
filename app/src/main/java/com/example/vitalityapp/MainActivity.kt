package com.example.vitalityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
// NAVIGATION
// ============================================

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Library : Screen("library")
    object Journal : Screen("journal")
    object Profile : Screen("profile")
}

@Composable
fun VitalityApp(dataStoreManager: DataStoreManager, viewModel: VitalityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val navController = rememberNavController()
    
    // Track current route for the bottom bar selection
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceLight,
        bottomBar = {
            VitalityBottomBar(currentRoute) { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(dataStoreManager) }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
            composable(Screen.Journal.route) { JournalScreen(viewModel) }
            composable(Screen.Profile.route) { ProfileScreen(viewModel) }
        }
    }
}

@Composable
fun VitalityBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val tabs = listOf(
        Triple("Home", Icons.Default.Home, Screen.Home.route),
        Triple("Insights", Icons.Default.Insights, Screen.Insights.route),
        Triple("Library", Icons.AutoMirrored.Filled.MenuBook, Screen.Library.route),
        Triple("Journal", Icons.Default.EditNote, Screen.Journal.route),
        Triple("Profile", Icons.Default.Person, Screen.Profile.route)
    )

    NavigationBar(containerColor = CardWhite, tonalElevation = 8.dp) {
        tabs.forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, label) },
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
// SCREENS
// ============================================

@Composable
fun LibraryScreen() {
    ScreenPlaceholder("Library Screen")
}

@Composable
fun InsightsScreen() {
    val weeklyData = listOf(
        60, 72, 65, 80, 75, 68, 82
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Insights", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Your progress this week", fontSize = 14.sp, color = TextSecondary)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Weekly Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = VitalityPurple)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Weekly Average", color = Color.White.copy(0.8f), fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("72", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("/100", color = Color.White.copy(0.6f), fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Text("+8%", color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                        Text("vs last week", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bar Chart Representation
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier
                                .width(24.dp)
                                .height((weeklyData[index] * 0.8f).dp)
                                .background(Color.White.copy(alpha = if (index == 6) 1f else 0.5f), RoundedCornerShape(4.dp))
                            )
                            Text(day, color = Color.White.copy(0.7f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Metric Breakdown", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        
        InsightMetricCard("🏃 Movement", 17, 25, "+3 from last week", VitalityBlue)
        InsightMetricCard("🥗 Nutrition", 21, 25, "+2 from last week", VitalityTeal)
        InsightMetricCard("😴 Sleep", 15, 25, "Same as last week", VitalityPurple)
        InsightMetricCard("🧘 Mood", 19, 25, "+3 from last week", VitalityPink)
    }
}

@Composable
fun InsightMetricCard(label: String, score: Int, max: Int, trend: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold)
                Text(trend, fontSize = 12.sp, color = TextSecondary)
            }
            Text("$score/$max", fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        }
    }
}

@Composable
fun JournalScreen(viewModel: VitalityViewModel) {
    var currentNote by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("") }
    val entries by viewModel.journalEntries.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Journal", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Track your thoughts and feelings", fontSize = 14.sp, color = TextSecondary)
        
        Spacer(modifier = Modifier.height(20.dp))

        // New Entry Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("How are you feeling?", fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    val moods = listOf("😊", "🌟", "😴", "🧘", "💪")
                    moods.forEach { mood ->
                        MoodButton(mood, isSelected = selectedMood == mood) { selectedMood = mood }
                    }
                }

                OutlinedTextField(
                    value = currentNote,
                    onValueChange = { currentNote = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    label = { Text("Write your thoughts...") },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Button(
                    onClick = {
                        viewModel.addJournalEntry(currentNote, selectedMood)
                        currentNote = ""
                        selectedMood = ""
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    enabled = currentNote.isNotBlank() && selectedMood.isNotBlank()
                ) { Text("Save Entry") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Past Entries", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        
        entries.forEach { entry ->
            JournalEntryCard(entry)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MoodButton(emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp, 40.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) VitalityPurple else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) VitalityPurple.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 20.sp)
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(entry.date, fontSize = 12.sp, color = TextSecondary)
                Box(modifier = Modifier.size(32.dp).background(VitalityPurple.copy(0.05f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(entry.mood)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(entry.content, fontSize = 15.sp)
        }
    }
}

@Composable
fun ProfileScreen(viewModel: VitalityViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).background(VitalityPurple, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = profile.name.take(1), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Member since Jan 2024", fontSize = 14.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
        
        getAchievements().chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { achievement ->
                    AchievementGridItem(achievement)
                }
                if (row.size < 3) {
                    repeat(3 - row.size) { 
                        Spacer(modifier = Modifier.weight(1f)) 
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
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
    Card(
        modifier = Modifier.width(0.dp).weight(1f).aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (achievement.isUnlocked) CardWhite else Color.Gray.copy(0.1f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = if (achievement.isUnlocked) achievement.emoji else "🔒", fontSize = 28.sp)
            Text(text = achievement.title, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SettingsItem(emoji: String, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
    }
}

data class Achievement(val title: String, val emoji: String, val isUnlocked: Boolean)
fun getAchievements() = listOf(
    Achievement("First Goal", "🎯", true),
    Achievement("7 Day Streak", "⚡", true),
    Achievement("Early Bird", "🌅", true),
    Achievement("Night Owl", "🌙", false),
    Achievement("Pro Athlete", "🏅", false)
)

@Composable
fun StatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScreenPlaceholder(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, style = MaterialTheme.typography.headlineMedium)
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
                            Text("/100", color = Color.White.copy(0.6f), fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
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
        elevation = CardDefaults.cardElevation(2.dp)
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
