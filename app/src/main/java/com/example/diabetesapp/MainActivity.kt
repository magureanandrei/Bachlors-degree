package com.example.diabetesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.diabetesapp.ui.components.BottomNavBar
import com.example.diabetesapp.ui.screens.*
import com.example.diabetesapp.ui.theme.DiabetesAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiabetesAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedRoute by remember { mutableStateOf("home") }
    var currentScreen by remember { mutableStateOf("home") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom nav on all main screens, hide only on detail screens
            if (currentScreen in listOf("home", "bolus", "history", "stats", "settings")) {
                BottomNavBar(
                    selectedRoute = selectedRoute,
                    onNavigate = { route -> 
                        selectedRoute = route
                        currentScreen = route
                    }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            "home" -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToCalculateBolus = { currentScreen = "calculate_bolus" }
            )
            "bolus" -> BolusScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToCalculateBolus = { currentScreen = "calculate_bolus" }
            )
            "history" -> HistoryScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToCalculateBolus = { currentScreen = "calculate_bolus" }
            )
            "stats" -> StatsScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToCalculateBolus = { currentScreen = "calculate_bolus" }
            )
            "settings" -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToCalculateBolus = { currentScreen = "calculate_bolus" }
            )
            "calculate_bolus" -> CalculateBolusScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateBack = { currentScreen = selectedRoute }
            )
            else -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToCalculateBolus = { currentScreen = "calculate_bolus" }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DiabetesAppTheme {
        MainScreen()
    }
}