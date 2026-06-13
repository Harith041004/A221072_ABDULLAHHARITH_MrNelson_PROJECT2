package com.example.vitalityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.vitalityapp.ui.theme.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Journal : Screen("journal")
    object Profile : Screen("profile")
    object Goals : Screen("goals")
    object PushUp : Screen("pushup")
    object Community : Screen("community")
}

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

@Composable
fun VitalityApp(dataStoreManager: DataStoreManager, viewModel: VitalityViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val savedData by dataStoreManager.getSettings.collectAsStateWithLifecycle(initialValue = null)
    
    LaunchedEffect(savedData) {
        savedData?.let {
            viewModel.initializeFromDataStore(it, dataStoreManager)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
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
            // We pass navigation commands to the HomeScreen here!
            composable(Screen.Home.route) { 
                HomeScreen(
                    dataStoreManager = dataStoreManager, 
                    viewModel = viewModel,
                    onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                    onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                ) 
            }
            composable(Screen.Insights.route) { InsightsScreen(viewModel) }
            composable(Screen.Journal.route) { JournalScreen(dataStoreManager, viewModel) }
            composable(Screen.Profile.route) { ProfileScreen(dataStoreManager, viewModel) }
            composable(Screen.Goals.route) { GoalsScreen(viewModel) }
            
            // Project 2 Screens
            composable(Screen.PushUp.route) { 
                val context = androidx.compose.ui.platform.LocalContext.current
                val database = androidx.room.Room.databaseBuilder(context, VitalityDatabase::class.java, "vitality-db").build()
                PushUpFlowScreen(pushUpDao = database.pushUpDao()) 
            }
            composable(Screen.Community.route) { CommunityScreen(viewModel) }
        }
    }
}

@Composable
fun VitalityBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    // ONLY 4 TABS - Clean and Professional!
    val tabs = listOf(
        Triple("Home", Icons.Default.Home, Screen.Home.route),
        Triple("Journal", Icons.AutoMirrored.Filled.MenuBook, Screen.Journal.route),
        Triple("Practice", Icons.Default.FitnessCenter, Screen.PushUp.route),
        Triple("Community", Icons.Default.Forum, Screen.Community.route)
    )

    NavigationBar(containerColor = SurfaceWhite, tonalElevation = 8.dp) {
        tabs.forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, label) },
                label = { Text(label, fontSize = 11.sp) }, // Back to normal font size!
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    indicatorColor = PrimaryPurple.copy(alpha = 0.1f)
                )
            )
        }
    }
}
