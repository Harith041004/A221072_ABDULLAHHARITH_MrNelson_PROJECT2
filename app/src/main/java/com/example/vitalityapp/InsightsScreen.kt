package com.example.vitalityapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitalityapp.ui.theme.*

@Composable
fun InsightsScreen() {
    val weeklyData = listOf(60, 72, 65, 80, 75, 68, 82)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(text = "Insights", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Your progress this week", fontSize = 14.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Average Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Weekly Average", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("72", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("/100", color = Color.White.copy(alpha = 0.6f), fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Text("+8%", color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                        Text("vs last week", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((weeklyData[index] * 0.8f).dp)
                                    .background(Color.White.copy(alpha = if (index == 6) 1f else 0.5f), RoundedCornerShape(4.dp))
                            )
                            Text(day, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
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
        InsightMetricCard("😴 Sleep", 15, 25, "Same as last week", PrimaryPurple)
        InsightMetricCard("🧘 Mood", 19, 25, "+3 from last week", VitalityPink)
    }
}

@Composable
fun InsightMetricCard(label: String, score: Int, max: Int, trend: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold)
                Text(trend, fontSize = 12.sp, color = TextSecondary)
            }
            Text("$score/$max", fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        }
    }
}
