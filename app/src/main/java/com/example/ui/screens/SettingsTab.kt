package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled._3dRotation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsTab(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val themeSelected by viewModel.currentTheme.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()

    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toolbar header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "APP CONFIGURATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Customize themes, telemetry alerts, and memory backups",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        }

        // 1. Premium Theme selector box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Premium Themes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Grid selectors
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeSelectionBubble(
                            title = "Modern Light",
                            themeVal = PreferencesManager.AppTheme.MODERN_LIGHT,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_modern_light"
                        )

                        ThemeSelectionBubble(
                            title = "Dark",
                            themeVal = PreferencesManager.AppTheme.DARK,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_dark"
                        )

                        ThemeSelectionBubble(
                            title = "AMOLED Black",
                            themeVal = PreferencesManager.AppTheme.PURE_BLACK,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_amoled_black"
                        )

                        ThemeSelectionBubble(
                            title = "Gold Elite",
                            themeVal = PreferencesManager.AppTheme.GOLD_ELITE,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_gold_elite"
                        )

                        ThemeSelectionBubble(
                            title = "Royal Blue",
                            themeVal = PreferencesManager.AppTheme.ROYAL_BLUE,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_royal_blue"
                        )

                        ThemeSelectionBubble(
                            title = "Emerald Green",
                            themeVal = PreferencesManager.AppTheme.EMERALD_GREEN,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_emerald_green"
                        )

                        ThemeSelectionBubble(
                            title = "Crimson Red",
                            themeVal = PreferencesManager.AppTheme.CRIMSON_RED,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_crimson_red"
                        )

                        ThemeSelectionBubble(
                            title = "Purple Neon",
                            themeVal = PreferencesManager.AppTheme.PURPLE_NEON,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_purple_neon"
                        )

                        ThemeSelectionBubble(
                            title = "Cyberpunk",
                            themeVal = PreferencesManager.AppTheme.CYBERPUNK,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_cyberpunk"
                        )

                        ThemeSelectionBubble(
                            title = "Vintage Card Table",
                            themeVal = PreferencesManager.AppTheme.VINTAGE_CARD_TABLE,
                            currentTheme = themeSelected,
                            onSelect = {
                                viewModel.playButtonClick()
                                viewModel.setTheme(it)
                            },
                            tag = "theme_vintage_card_table"
                        )
                    }
                }
            }
        }

        // 2. Sound & Haptic toggle configurations
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Sounds and Haptics",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Sound Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Sound Effects",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Play tone when score is logged or won",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                viewModel.playButtonClick()
                                viewModel.setSoundEnabled(it)
                            },
                            modifier = Modifier.testTag("sound_toggle_switch")
                        )
                    }

                    // Haptic Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Haptic Feedback",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Vibrate on button score inputs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = {
                                viewModel.playButtonClick()
                                viewModel.setHapticEnabled(it)
                            },
                            modifier = Modifier.testTag("haptic_toggle_switch")
                        )
                    }
                }
            }
        }

        // 3. Database Maintenance Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFC0392B),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Database Maintenance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFC0392B)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    Text(
                        text = "Wiping the match history clears all previous files forever. This action is irreversible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.playButtonClick()
                            showClearHistoryConfirm = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("clear_history_master_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC0392B)),
                        border = BorderStroke(1.2.dp, Color(0xFFC0392B).copy(alpha = 0.4f))
                    ) {
                        Text("CLEAR MATCH HISTORY", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold))
                    }
                }
            }
        }

        // 4. Information card
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "🇮🇹 Score Italy card game scoreboard tracker",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Version: 1.0.0 (Stable Release)  •  100% Offline with SQLite  •  Privacy First, No telemetry or cloud syncing pipelines.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Made with ♥ by Saleh Azgor Rishad",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Clear confirmation Dialog
        item {
            if (showClearHistoryConfirm) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryConfirm = false },
                    title = { Text("Erase All History?") },
                    text = { Text("Wipe the local SQLite data completely? All saved card scoreboards and charts telemetry will be immediately cleared from this device and cannot be recovered.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearAllHistory()
                                showClearHistoryConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCD212A))
                        ) {
                            Text("Erase All")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showClearHistoryConfirm = false }) {
                            Text("Cancel")
                        }
                    },
                    modifier = Modifier.testTag("clear_history_confirm_dialog")
                )
            }
        }
    }
}

@Composable
fun ThemeSelectionBubble(
    title: String,
    themeVal: PreferencesManager.AppTheme,
    currentTheme: PreferencesManager.AppTheme,
    onSelect: (PreferencesManager.AppTheme) -> Unit,
    tag: String
) {
    val isSelected = currentTheme == themeVal
    val cardColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.2.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        modifier = Modifier
            .clickable { onSelect(themeVal) }
            .testTag(tag)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}
