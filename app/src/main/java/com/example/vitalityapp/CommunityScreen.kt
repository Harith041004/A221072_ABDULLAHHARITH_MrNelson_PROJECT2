package com.example.vitalityapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitalityapp.ui.theme.*

@Composable
fun CommunityScreen(viewModel: VitalityViewModel) {
    val dailyQuote by viewModel.dailyQuote.collectAsStateWithLifecycle() 
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    var postText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Community Board", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        
        // Pillar 3: Data from Internet (Retrofit API)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), 
            colors = CardDefaults.cardColors(containerColor = SecondaryTeal)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Inspiration of the Day", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("\"${dailyQuote?.q ?: "Loading..."}\"", color = Color.White, fontWeight = FontWeight.Bold)
                Text("- ${dailyQuote?.a ?: ""}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        // Pillar 2: Cloud Integration (Firebase Write)
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Share your progress...") },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = { 
                    viewModel.publishToFirebase(postText)
                    postText = "" // Clear the text box after sending
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Post", tint = PrimaryPurple)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pillar 2: Cloud Integration (Firebase Read)
        LazyColumn {
            items(communityPosts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(post.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryPurple)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(post.message, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
