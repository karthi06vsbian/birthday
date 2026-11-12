package com.gaming.companion

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gaming.companion.aimtraining.AimTrainingScreen
import com.gaming.companion.aimtraining.AimTrainingViewModel
import com.gaming.companion.dashboard.DashboardScreen
import com.gaming.companion.dashboard.DashboardViewModel
import com.gaming.companion.radar.RadarScreen
import com.gaming.companion.radar.RadarViewModel
import com.gaming.companion.settings.SettingsScreen
import com.gaming.companion.settings.SettingsViewModel
import com.gaming.companion.streamer.StreamerScreen
import com.gaming.companion.streamer.StreamerViewModel
import com.gaming.companion.ui.components.BottomNavBar
import com.gaming.companion.ui.components.Screen
import com.gaming.companion.ui.theme.BgDark
import com.gaming.companion.ui.theme.FFGamingCompanionTheme

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val aimViewModel: AimTrainingViewModel by viewModels()
    private val radarViewModel: RadarViewModel by viewModels()
    private val streamerViewModel: StreamerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup FLAG_SECURE window listener for Streamer Privacy Mode
        streamerViewModel.onFlagSecureToggled = { isEnabled ->
            runOnUiThread {
                if (isEnabled) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        setContent {
            FFGamingCompanionTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgDark,
                    bottomBar = {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToAim = { navController.navigate(Screen.AimTraining.route) },
                                onNavigateToRadar = { navController.navigate(Screen.Radar.route) },
                                onNavigateToStreamer = { navController.navigate(Screen.Streamer.route) },
                                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                            )
                        }
                        composable(Screen.AimTraining.route) {
                            AimTrainingScreen(viewModel = aimViewModel)
                        }
                        composable(Screen.Radar.route) {
                            RadarScreen(viewModel = radarViewModel)
                        }
                        composable(Screen.Streamer.route) {
                            StreamerScreen(viewModel = streamerViewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}
