package com.example.vitalityapp

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vitalityapp.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

enum class PushUpState {
    SELECTION, COUNTDOWN, TRACKING, SUMMARY, HISTORY
}

data class SessionResult(val reps: Int, val durationSeconds: Int)

@Composable
fun PushUpFlowScreen(pushUpDao: PushUpDao) {
    var currentState by remember { mutableStateOf(PushUpState.SELECTION) }
    var selectedMode by remember { mutableStateOf("Free Practice") }
    var finalResult by remember { mutableStateOf(SessionResult(0, 0)) }

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    when (currentState) {
        PushUpState.SELECTION -> {
            PushUpSelectionScreen(
                onModeSelected = { mode ->
                    selectedMode = mode
                    currentState = PushUpState.COUNTDOWN
                },
                onViewHistory = { currentState = PushUpState.HISTORY }
            )
        }
        PushUpState.COUNTDOWN -> {
            PushUpCountdownScreen(
                onSkip = { currentState = PushUpState.TRACKING },
                onFinish = { currentState = PushUpState.TRACKING }
            )
        }
        PushUpState.TRACKING -> {
            PushUpCameraTrackingScreen(
                mode = selectedMode,
                hasPermission = hasCameraPermission,
                onEndSession = { result ->
                    finalResult = result
                    currentState = PushUpState.SUMMARY
                }
            )
        }
        PushUpState.SUMMARY -> {
            PushUpSummaryScreen(
                result = finalResult,
                pushUpDao = pushUpDao,
                onSaveAndExit = { currentState = PushUpState.SELECTION }
            )
        }
        PushUpState.HISTORY -> {
            PushUpHistoryScreen(
                pushUpDao = pushUpDao,
                onBack = { currentState = PushUpState.SELECTION }
            )
        }
    }
}

// --- STEP 1: SELECTION UI ---
@Composable
fun PushUpSelectionScreen(onModeSelected: (String) -> Unit, onViewHistory: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(16.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Practice", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            
            // HISTORY BUTTON
            TextButton(onClick = onViewHistory) {
                Icon(Icons.Default.History, contentDescription = "History", tint = PrimaryPurple)
                Spacer(modifier = Modifier.width(4.dp))
                Text("History", color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        PracticeOptionCard(icon = "∞", title = "OPEN GOAL", subtitle = "Free Practice", iconTint = PrimaryPurple) { onModeSelected("Free Practice") }
        PracticeOptionCard(icon = "⏱", title = "TIMER", subtitle = "1m exercise", iconTint = PrimaryPurple) { onModeSelected("Timer") }
        PracticeOptionCard(icon = "◎", title = "LIMIT", subtitle = "10 push-ups", iconTint = PrimaryPurple) { onModeSelected("Limit") }
    }
}

@Composable
fun PracticeOptionCard(icon: String, title: String, subtitle: String, iconTint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(iconTint.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 24.sp, color = iconTint, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = TextSecondary, fontSize = 14.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

// --- STEP 2: COUNTDOWN UI ---
@Composable
fun PushUpCountdownScreen(onSkip: () -> Unit, onFinish: () -> Unit) {
    var count by remember { mutableIntStateOf(3) }
    LaunchedEffect(Unit) {
        while (count > 0) {
            delay(1000)
            count--
        }
        onFinish()
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("GET READY", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(48.dp))
        Box(modifier = Modifier.size(200.dp).border(8.dp, PrimaryPurple.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { count / 3f }, modifier = Modifier.fillMaxSize(), color = PrimaryPurple, strokeWidth = 8.dp, trackColor = Color.Transparent)
            Text("$count", color = PrimaryPurple, fontSize = 80.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(64.dp))
        OutlinedButton(
            onClick = onSkip, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, PrimaryPurple), colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple)
        ) { Text("SKIP", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
    }
}

// --- STEP 3: ML KIT CAMERA TRACKING UI ---
@Composable
fun PushUpCameraTrackingScreen(mode: String, hasPermission: Boolean, onEndSession: (SessionResult) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var reps by remember { mutableIntStateOf(0) }
    var isDown by remember { mutableStateOf(false) }
    var timeElapsed by remember { mutableIntStateOf(0) }

    // 1. Auto-Stop for TIMER mode (1 minute)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeElapsed++
            if (mode == "Timer" && timeElapsed >= 60) {
                onEndSession(SessionResult(reps, timeElapsed))
                break
            }
        }
    }

    // 2. Auto-Stop for LIMIT mode (10 Reps)
    LaunchedEffect(reps) {
        if (mode == "Limit" && reps >= 10) {
            onEndSession(SessionResult(reps, timeElapsed))
        }
    }

    val faceDetector = remember {
        val options = FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).build()
        FaceDetection.getClient(options)
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val speed = if (reps > 0) String.format("%.2f", timeElapsed.toFloat() / reps) else "0.00"
    val estDistance = reps * 18
    val timeFormatted = String.format("%02d:%02d", timeElapsed / 60, timeElapsed % 60)

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Large Timer Display
        Text(timeFormatted, color = TextPrimary, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        Spacer(modifier = Modifier.height(24.dp))

        if (hasPermission) {
            Box(modifier = Modifier.size(260.dp).clip(CircleShape).background(Color.LightGray).border(6.dp, PrimaryPurple, CircleShape)) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            val imageAnalyzer = ImageAnalysis.Builder().setTargetResolution(Size(480, 640)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    @OptIn(ExperimentalGetImage::class)
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                        faceDetector.process(image).addOnSuccessListener { faces ->
                                            if (faces.isNotEmpty()) {
                                                val faceHeight = faces.first().boundingBox.height()
                                                if (faceHeight > 350) isDown = true
                                                else if (faceHeight < 250 && isDown) {
                                                    reps++
                                                    isDown = false
                                                }
                                            }
                                        }.addOnCompleteListener { imageProxy.close() }
                                    }
                                }
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalyzer)
                            } catch (e: Exception) { e.printStackTrace() }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Live Metrics Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val repsDisplay = if (mode == "Limit") "$reps / 10" else "$reps"
            MetricItem("REPS", repsDisplay)
            MetricItem("SPEED", "${speed}s")
            MetricItem("DIST.", "${estDistance}cm")
        }

        Spacer(modifier = Modifier.weight(1f))

        // End Button
        Button(
            onClick = { onEndSession(SessionResult(reps, timeElapsed)) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) { Text("END", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 20.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
    }
}

// --- STEP 4: SUMMARY UI ---
@Composable
fun PushUpSummaryScreen(result: SessionResult, pushUpDao: PushUpDao, onSaveAndExit: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    val speed = if (result.reps > 0) String.format("%.2f", result.durationSeconds.toFloat() / result.reps) else "0.00"
    val avgRepDistance = 18 // cm
    val strictReps = result.reps 
    val totalDistance = result.reps * avgRepDistance

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Congratulations", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.size(100.dp).background(SuccessGreen, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Workout Completed", fontSize = 16.sp, color = TextSecondary)
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
                Text("Workout Session", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                SummaryRow(Icons.Default.Flag, "TOTAL PUSH-UPS", "${result.reps}")
                SummaryRow(Icons.Default.Timer, "TOTAL DURATION", "${result.durationSeconds}s")
                SummaryRow(Icons.Default.Speed, "SPEED", "${speed}s")
                SummaryRow(Icons.Default.Straighten, "AVG. REP DISTANCE", "${avgRepDistance}cm")
                SummaryRow(Icons.Default.SportsScore, "STRICT REPS COUNT", "$strictReps")
                SummaryRow(Icons.Default.Height, "TOTAL REP DISTANCE", "${totalDistance}cm")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch { pushUpDao.insertSession(PushUpSession(reps = result.reps, durationSeconds = result.durationSeconds)) }
                onSaveAndExit()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) { Text("SAVE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun SummaryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(BackgroundLight, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun PushUpHistoryScreen(pushUpDao: PushUpDao, onBack: () -> Unit) {
    val sessions by pushUpDao.getAllSessions().collectAsState(initial = emptyList<PushUpSession>())

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryPurple)
            }
            Text("Push-Up History", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sessions found", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: PushUpSession) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(session.dateInMillis))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(PrimaryPurple.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.FitnessCenter, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${session.reps} Push-ups", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(dateStr, fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
