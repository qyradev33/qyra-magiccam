package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.ModStoreScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost() {
    val navController = rememberNavController()
    val viewModel: CameraViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "camera",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("camera") {
            CameraScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToModStore = { navController.navigate("modstore") },
                onNavigateToGallery = { navController.navigate("gallery") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("modstore") {
            ModStoreScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("gallery") {
            GalleryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
