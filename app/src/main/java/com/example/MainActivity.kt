package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.HistoryTab
import com.example.ui.screens.MatchTab
import com.example.ui.screens.SettingsTab
import com.example.ui.screens.StatisticsTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()

            MyApplicationTheme(themeType = currentTheme) {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab {
    MATCH,
    HISTORY,
    STATISTICS,
    SETTINGS
}

@Composable
fun MainAppScaffold(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(NavigationTab.MATCH) }
    val activeMatch by viewModel.activeMatch.collectAsState()

    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Scaffold(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxHeight(),
                topBar = {
                    AppHeader(activeMatch = activeMatch, activeTab = activeTab)
                },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F0F0F),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_bar")
            ) {
                val navBarColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color(0x14FFFFFF)
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.MATCH,
                    onClick = {
                        if (activeTab != NavigationTab.MATCH) {
                            viewModel.playSwipeSound()
                            activeTab = NavigationTab.MATCH
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Match Scoreboard screen"
                        )
                    },
                    label = { Text("Match") },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_match")
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.HISTORY,
                    onClick = {
                        if (activeTab != NavigationTab.HISTORY) {
                            viewModel.playSwipeSound()
                            activeTab = NavigationTab.HISTORY
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Match score logs history list"
                        )
                    },
                    label = { Text("History") },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.STATISTICS,
                    onClick = {
                        viewModel.playButtonClick()
                        activeTab = NavigationTab.STATISTICS
                    },
                    icon = {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Match stats diagnostics summary"
                        )
                    },
                    label = { Text("Statistics") },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_statistics")
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.SETTINGS,
                    onClick = {
                        if (activeTab != NavigationTab.SETTINGS) {
                            viewModel.playSwipeSound()
                            activeTab = NavigationTab.SETTINGS
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "App settings and customization"
                        )
                    },
                    label = { Text("Settings") },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                NavigationTab.MATCH -> {
                    MatchTab(
                        viewModel = viewModel,
                        onNavigateToHistory = { activeTab = NavigationTab.HISTORY }
                    )
                }
                NavigationTab.HISTORY -> {
                    HistoryTab(viewModel = viewModel)
                }
                NavigationTab.STATISTICS -> {
                    StatisticsTab(viewModel = viewModel)
                }
                NavigationTab.SETTINGS -> {
                    SettingsTab(viewModel = viewModel)
                }
            }
        }
    }
        }
    }
}

@Composable
fun AppHeader(
    activeMatch: GameViewModel.ActiveMatch,
    activeTab: NavigationTab
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF0F0F0F))
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "SCORE ITALY",
                style = androidx.compose.ui.text.TextStyle(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )

            if (activeMatch.isMatchStarted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFCD212A).copy(alpha = pulseAlpha), CircleShape)
                    )
                    Text(
                        text = "Match in progress...",
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            } else {
                Text(
                    text = when (activeTab) {
                        NavigationTab.MATCH -> "New Card Match"
                        NavigationTab.HISTORY -> "Match Log Archives"
                        NavigationTab.STATISTICS -> "Performance Stats"
                        NavigationTab.SETTINGS -> "Preferences"
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                )
            }
        }

        // Italian Flag Minimalist Accent Badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFF008C45))) // Green
            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFFF4F5F0))) // White
            Box(modifier = Modifier.size(width = 8.dp, height = 12.dp).background(Color(0xFFCD212A))) // Red
        }
    }
}

private fun formatDurationHMS(durationSeconds: Long): String {
    val hrs = durationSeconds / 3600
    val mins = (durationSeconds % 3600) / 60
    val secs = durationSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}
