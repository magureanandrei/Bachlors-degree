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
import com.example.diabetesapp.ui.screens.HomeScreen
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                selectedRoute = selectedRoute,
                onNavigate = { route -> selectedRoute = route }
            )
        }
    ) { innerPadding ->
        when (selectedRoute) {
            "home" -> HomeScreen(modifier = Modifier.padding(innerPadding))
            else -> HomeScreen(modifier = Modifier.padding(innerPadding)) // Placeholder for other screens
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