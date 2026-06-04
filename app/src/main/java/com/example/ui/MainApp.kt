package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.detail.DetailScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.scan.ScanScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.TrueScanTheme

enum class TrueScanTab {
    SCAN, SEARCH, HISTORY, SETTINGS
}

@Composable
fun MainApp(
    viewModel: TrueScanViewModel,
    modifier: Modifier = Modifier
) {
    // Determine system / pref theme
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    TrueScanTheme(darkTheme = isDark) {
        var currentTab by remember { mutableStateOf(TrueScanTab.SCAN) }
        var selectedBarcode by remember { mutableStateOf<String?>(null) }

        // Handle native system back button when detailed overview is visible
        if (selectedBarcode != null) {
            BackHandler {
                selectedBarcode = null
                viewModel.clearProductState()
            }
        }

        Scaffold(
            bottomBar = {
                if (selectedBarcode == null) {
                    NavigationBar(
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("app_bottom_bar")
                    ) {
                        // 1. Scan Tab
                        NavigationBarItem(
                            selected = currentTab == TrueScanTab.SCAN,
                            onClick = { currentTab = TrueScanTab.SCAN },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TrueScanTab.SCAN) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                                    contentDescription = "Scan"
                                )
                            },
                            label = { Text("Scan") },
                            modifier = Modifier.testTag("nav_scan")
                        )

                        // 2. Search Tab
                        NavigationBarItem(
                            selected = currentTab == TrueScanTab.SEARCH,
                            onClick = { currentTab = TrueScanTab.SEARCH },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TrueScanTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text("Search") },
                            modifier = Modifier.testTag("nav_search")
                        )

                        // 3. History Tab
                        NavigationBarItem(
                            selected = currentTab == TrueScanTab.HISTORY,
                            onClick = { 
                                currentTab = TrueScanTab.HISTORY 
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TrueScanTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                    contentDescription = "Logs"
                                )
                            },
                            label = { Text("Logs") },
                            modifier = Modifier.testTag("nav_logs")
                        )

                        // 4. Settings Tab
                        NavigationBarItem(
                            selected = currentTab == TrueScanTab.SETTINGS,
                            onClick = { currentTab = TrueScanTab.SETTINGS },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TrueScanTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings") },
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedBarcode != null) {
                    DetailScreen(
                        barcode = selectedBarcode!!,
                        viewModel = viewModel,
                        onBack = {
                            selectedBarcode = null
                            viewModel.clearProductState()
                        }
                    )
                } else {
                    // Page Content selection
                    when (currentTab) {
                        TrueScanTab.SCAN -> {
                            ScanScreen(
                                viewModel = viewModel,
                                onNavigateToResult = { barcode ->
                                    selectedBarcode = barcode
                                }
                            )
                        }
                        TrueScanTab.SEARCH -> {
                            SearchScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { barcode ->
                                    selectedBarcode = barcode
                                }
                            )
                        }
                        TrueScanTab.HISTORY -> {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { barcode ->
                                    selectedBarcode = barcode
                                }
                            )
                        }
                        TrueScanTab.SETTINGS -> {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
