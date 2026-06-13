package com.example.vitalityapp

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: VitalityViewModel) {
    val goals by viewModel.healthGoals.collectAsStateWithLifecycle()
    val dailyScore by viewModel.dailyScore.collectAsStateWithLifecycle()
    val showAddGoalDialog by viewModel.showAddGoalDialog.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundLight)
    ) {
        // Header with Score
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(VitalityPurple, VitalityPurpleLight)))
                .padding(24.dp)
        ) {
            Column {
                Text("My Health Goals", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Track your progress and stay motivated", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Daily Score Card in Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { dailyScore / 100f },
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 4.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text("$dailyScore", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Daily Vitality Score", color = Color.White, fontSize = 12.sp)
                            Text("Complete your habits to earn points!", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        
        // Add Goal Button
        Button(
            onClick = { viewModel.setShowAddGoalDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Goal")
        }
        
        // Goals List
        if (goals.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Goals Yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Tap 'Add New Goal' to start tracking", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                goals.forEach { goal ->
                    GoalCard(goal = goal, viewModel = viewModel)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(viewModel = viewModel)
    }
}

@Composable
fun GoalCard(goal: HealthGoal, viewModel: VitalityViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val progress = (goal.currentValue.toFloat() / goal.targetValue.toFloat()).coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Goal Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                val categoryColor = when (goal.category.lowercase()) {
                    "movement" -> VitalityBlue
                    "nutrition" -> VitalityTeal
                    "sleep" -> VitalityPurple
                    "mood" -> VitalityPink
                    else -> VitalityOrange
                }
                val categoryIcon = when (goal.category.lowercase()) {
                    "movement" -> Icons.AutoMirrored.Filled.DirectionsRun
                    "nutrition" -> Icons.Default.Restaurant
                    "sleep" -> Icons.Default.Bedtime
                    "mood" -> Icons.Default.SelfImprovement
                    else -> Icons.Default.TrackChanges
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            goal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (goal.isAchieved) {
                            Surface(
                                shape = CircleShape,
                                color = SuccessGreen.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎉", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Achieved!", fontSize = 10.sp, color = SuccessGreen)
                                }
                            }
                        }
                    }
                    Text(
                        "${goal.currentValue} / ${goal.targetValue}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isAchieved) SuccessGreen else PrimaryPurple,
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            
            // Progress Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progress", fontSize = 10.sp, color = TextSecondary)
                Text("${(progress * 100).toInt()}%", fontSize = 10.sp, color = TextSecondary)
            }
            
            // Expanded Content
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Deadline
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deadline: ${goal.deadline}", fontSize = 12.sp, color = TextSecondary)
                    }
                    
                    // Motivational Note
                    if (goal.motivationalNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(PrimaryPurple.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(goal.motivationalNote, fontSize = 12.sp, color = PrimaryPurple)
                            }
                        }
                    }
                    
                    // Quick Update Buttons
                    if (!goal.isAchieved) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Quick Update", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val step = (goal.targetValue / 5).coerceAtLeast(1)
                            listOf(step, step * 2, step * 3).forEach { increment ->
                                OutlinedButton(
                                    onClick = {
                                        val newValue = (goal.currentValue + increment).coerceAtMost(goal.targetValue)
                                        viewModel.updateHealthGoal(goal.id, newValue)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+$increment", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            // Expand/Collapse Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Show less" else "Show more",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(viewModel: VitalityViewModel) {
    val title by viewModel.newGoalTitleInput.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.newGoalCategoryInput.collectAsStateWithLifecycle()
    val targetValue by viewModel.newGoalTargetInput.collectAsStateWithLifecycle()
    val deadline by viewModel.newGoalDeadlineInput.collectAsStateWithLifecycle()
    val motivationalNote by viewModel.newGoalNoteInput.collectAsStateWithLifecycle()
    
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Movement", "Nutrition", "Sleep", "Mood")
    
    Dialog(onDismissRequest = { viewModel.setShowAddGoalDialog(false) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(SurfaceWhite)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Set a New Goal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Define what you want to achieve", fontSize = 12.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.updateNewGoalTitle(it) },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g., Walk 10,000 steps") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = { 
                                    viewModel.updateNewGoalCategory(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { viewModel.updateNewGoalTarget(it) },
                    label = { Text("Target Value") },
                    placeholder = { Text("e.g., 10000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { viewModel.updateNewGoalDeadline(it) },
                    label = { Text("Deadline (YYYY-MM-DD)") },
                    placeholder = { Text("e.g., 2024-12-31") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = motivationalNote,
                    onValueChange = { viewModel.updateNewGoalNote(it) },
                    label = { Text("Motivational Note (Optional)") },
                    placeholder = { Text("Stay motivated with a personal message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setShowAddGoalDialog(false) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val target = targetValue.toIntOrNull() ?: 100
                            val newGoal = HealthGoal(
                                title = title.ifBlank { "New Health Goal" },
                                category = selectedCategory,
                                targetValue = target,
                                deadline = deadline.ifBlank { "2024-12-31" },
                                motivationalNote = motivationalNote
                            )
                            viewModel.addHealthGoal(newGoal)
                            viewModel.setShowAddGoalDialog(false)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
