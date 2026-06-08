package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.model.Round
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.ConfettiEffect
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchTab(
    viewModel: GameViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMatch by viewModel.activeMatch.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Dialog state controllers
    var showResetDetailsConfirm by remember { mutableStateOf(false) }
    var selectedTeamForScoring by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (!activeMatch.isMatchStarted) {
            // ==========================================
            // CREATE MATCH SCREEN
            // ==========================================
            CreateMatchLayout(
                onStartMatch = { teamA, teamB, target ->
                    viewModel.startNewMatch(teamA, teamB, target)
                },
                playButtonClick = { viewModel.playButtonClick() }
            )
        } else {
            // ==========================================
            // ACTIVE SCOREBOARD SCREEN
            // ==========================================
            ActiveScoreboardLayout(
                activeMatch = activeMatch,
                onScoreEntryRequested = { teamName ->
                    viewModel.playButtonClick()
                    selectedTeamForScoring = teamName
                },
                onUndo = { viewModel.undoLastRound() },
                onResetClick = {
                    viewModel.playButtonClick()
                    showResetDetailsConfirm = true
                },
                onEndSaveClick = {
                    viewModel.playButtonClick()
                    viewModel.endAndSaveMatch()
                    onNavigateToHistory()
                }
            )
        }

        // Score Entry Panel Modal/Dialog (Slides over screen nicely, fast responsive overlay)
        if (selectedTeamForScoring != null) {
            val teamName = selectedTeamForScoring!!
            ScoreEntryOverlay(
                teamName = teamName,
                onDismiss = {
                    viewModel.playButtonClick()
                    selectedTeamForScoring = null
                },
                onApplyScore = { change ->
                    viewModel.addRoundScore(teamName, change)
                    selectedTeamForScoring = null
                },
                playButtonClick = { viewModel.playButtonClick() }
            )
        }

        // Winner Dialog with Confetti
        if (activeMatch.winnerName != null && !activeMatch.winnerAcknowledged) {
            WinnerCelebrationDialog(
                winnerName = activeMatch.winnerName!!,
                onContinue = { viewModel.continueMatchAfterWinner() },
                onEndMatch = {
                    viewModel.endAndSaveMatch()
                    onNavigateToHistory()
                }
            )
        }

        // Reset Confirmation Dialog
        if (showResetDetailsConfirm) {
            AlertDialog(
                onDismissRequest = { showResetDetailsConfirm = false },
                title = { Text("Reset Match?") },
                text = { Text("Are you sure you want to discard the current match and all its recorded rounds? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetActiveMatch()
                            showResetDetailsConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showResetDetailsConfirm = false }) {
                        Text("Cancel")
                    }
                },
                modifier = Modifier.testTag("reset_confirm_dialog")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateMatchLayout(
    onStartMatch: (String, String, Int) -> Unit,
    playButtonClick: () -> Unit
) {
    var teamAName by remember { mutableStateOf("") }
    var teamBName by remember { mutableStateOf("") }
    var targetScoreInput by remember { mutableStateOf("40") }

    // Standard pre-defined target score buttons
    val presetScores = listOf(40, 60, 80, 100, 120, 150)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Italian Accent Flag top banner
        item {
            ItalianFlagAccentBanner()
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SCORE ITALY",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Offline Card Game Score Tracker",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Customize Teams",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = teamAName,
                        onValueChange = { teamAName = it },
                        label = { Text("Team A Name") },
                        placeholder = { Text("Team A") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("team_a_input")
                    )

                    OutlinedTextField(
                        value = teamBName,
                        onValueChange = { teamBName = it },
                        label = { Text("Team B Name") },
                        placeholder = { Text("Team B") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("team_b_input")
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Target Score Limit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = targetScoreInput,
                        onValueChange = { targetScoreInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Winning Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_score_input")
                    )

                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetScores.forEach { score ->
                            val isSelected = targetScoreInput == score.toString()
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        playButtonClick()
                                        targetScoreInput = score.toString()
                                    }
                                    .testTag("preset_target_$score")
                            ) {
                                Text(
                                    text = "$score pts",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    playButtonClick()
                    val finalA = teamAName.trim().ifEmpty { "Team A" }
                    val finalB = teamBName.trim().ifEmpty { "Team B" }
                    val finalTarget = targetScoreInput.toIntOrNull() ?: 40
                    onStartMatch(finalA, finalB, finalTarget)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_match_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START CARD MATCH",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ActiveScoreboardLayout(
    activeMatch: GameViewModel.ActiveMatch,
    onScoreEntryRequested: (String) -> Unit,
    onUndo: () -> Unit,
    onResetClick: () -> Unit,
    onEndSaveClick: () -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeMatch.lastTimerStartTimeMs) {
        if (activeMatch.lastTimerStartTimeMs != null) {
            while (isActive) {
                delay(1000)
                now = System.currentTimeMillis()
            }
        }
    }
    val historyListState = rememberLazyListState()

    // Scroll automatically to the newest rounds added
    LaunchedEffect(activeMatch.rounds.size) {
        if (activeMatch.rounds.isNotEmpty()) {
            historyListState.animateScrollToItem(activeMatch.rounds.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Toolbar: Timer & Score limit indication
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Match Duration Timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Match duration clock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDuration(activeMatch.getCurrentDurationSeconds(now)),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("match_timer")
                    )
                }

                // Target Score Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Winning target score line",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Target: ${activeMatch.targetScore} pts",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ==========================================
        // DYNAMIC PRESTIGE SCOREBOARD CARDS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Team A Card
            val isALeading = activeMatch.teamAScore > activeMatch.teamBScore
            val isBLeading = activeMatch.teamBScore > activeMatch.teamAScore
            val isTie = activeMatch.teamAScore == activeMatch.teamBScore

            ScoreboardCard(
                teamName = activeMatch.teamAName,
                score = activeMatch.teamAScore,
                isLeading = isALeading && !isTie,
                streak = activeMatch.streakTeamA,
                onClick = { onScoreEntryRequested(activeMatch.teamAName) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("team_a_score_card")
            )

            // Team B Card
            ScoreboardCard(
                teamName = activeMatch.teamBName,
                score = activeMatch.teamBScore,
                isLeading = isBLeading && !isTie,
                streak = activeMatch.streakTeamB,
                onClick = { onScoreEntryRequested(activeMatch.teamBName) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("team_b_score_card")
            )
        }

        // Action Toolbar: Undo, Reset, End/Save
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Undo Utility Button
            OutlinedButton(
                onClick = onUndo,
                enabled = activeMatch.rounds.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("undo_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo last round", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Reset Utility Button
            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("reset_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Discard and restart match", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Complete Save Button
            Button(
                onClick = onEndSaveClick,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("complete_match_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save final scorecard results", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("End Match", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        // ==========================================
        // ROUNDS HISTORY SCROLLABLE COMPARTMENT
        // ==========================================
        Text(
            text = "Round Logging History",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            if (activeMatch.rounds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Empty round entries log list icon placeholder",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No scores recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Tap a team above to enter points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = historyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeMatch.rounds) { round ->
                        RoundHistoryRowItem(
                            round = round,
                            teamAName = activeMatch.teamAName,
                            teamBName = activeMatch.teamBName
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreboardCard(
    teamName: String,
    score: Int,
    isLeading: Boolean,
    streak: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLeading) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.5.dp,
            if (isLeading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLeading) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hot Streak Badge or Leader Badge or Spacer
            if (streak >= 3) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)), // Vibrant Orange-Red
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "🔥 x$streak STREAK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else if (isLeading) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "LEADER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Customized Team Label
            Text(
                text = teamName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isLeading) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scoreboard display values. Huge numeric size
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                ),
                color = if (isLeading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick add trigger hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = (if (isLeading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isLeading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Score",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RoundHistoryRowItem(
    round: Round,
    teamAName: String,
    teamBName: String
) {
    val isTeamA = round.teamName == teamAName
    val cardColor = if (isTeamA) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
    }

    val sideBorder = if (isTeamA) {
        BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    } else {
        BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = sideBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block description
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = (if (isTeamA) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${round.roundNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isTeamA) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = round.teamName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Delta difference
            val scoreString = if (round.scoreChange >= 0) "+${round.scoreChange}" else "${round.scoreChange}"
            val scoreColor = if (round.scoreChange >= 0) {
                Color(0xFF008C45) // Beautiful Italian Green
            } else {
                Color(0xFFCD212A) // Beautiful Italian Red
            }

            Text(
                text = scoreString,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = scoreColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoreEntryOverlay(
    teamName: String,
    onDismiss: () -> Unit,
    onApplyScore: (Int) -> Unit,
    playButtonClick: () -> Unit
) {
    var rawCustomScoreInput by remember { mutableStateOf("") }
    var applyIsNegative by remember { mutableStateOf(false) }

    // Pre-configured point constants requested standardly
    val positivePads = listOf(5, 6, 7, 8, 9, 10, 11, 12, 13)
    val negativePads = listOf(5, 6, 7, 8, 9, 10)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Points: $teamName",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_score_panel")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close entry overlay panel")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Positive Increment Options (+5 to +13)
                    item {
                        Text(
                            text = "Positive Score Options",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF34D399)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            positivePads.forEach { points ->
                                val isSpecial = points == 10
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSpecial) Color(0xFF10B981) else Color(0x1F10B981)
                                    ),
                                    border = if (isSpecial) null else BorderStroke(1.dp, Color(0x5010B981)),
                                    modifier = Modifier
                                        .clickable {
                                            playButtonClick()
                                            onApplyScore(points)
                                        }
                                        .testTag("score_pad_positive_$points")
                                        .widthIn(min = 68.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isSpecial) "BASE" else "ADD",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (isSpecial) Color.Black.copy(alpha = 0.6f) else Color(0xFF34D399).copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+$points",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = if (isSpecial) Color.Black else Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Negative Decrement Options (-5 to -10)
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Negative Score Options",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFB7185)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            negativePads.forEach { points ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0x1FCD212A)
                                    ),
                                    border = BorderStroke(1.dp, Color(0x50CD212A)),
                                    modifier = Modifier
                                        .clickable {
                                            playButtonClick()
                                            onApplyScore(-points)
                                        }
                                        .testTag("score_pad_negative_$points")
                                        .widthIn(min = 68.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "SUBTRACT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                letterSpacing = 0.8.sp
                                            ),
                                            color = Color(0xFFFB7185).copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "-$points",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = Color(0xFFFB7185)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Alternative Input: Custom Score TextField
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Alternative Manual Input",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Negative Sign toggle
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (applyIsNegative) Color(0xFFCD212A) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        playButtonClick()
                                        applyIsNegative = !applyIsNegative
                                    }
                                    .size(48.dp)
                                    .testTag("score_entry_negative_toggle"),
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                        color = if (applyIsNegative) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = rawCustomScoreInput,
                                onValueChange = { rawCustomScoreInput = it.filter { c -> c.isDigit() } },
                                label = { Text("Custom Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("score_entry_custom_field")
                            )

                            Button(
                                onClick = {
                                    playButtonClick()
                                    val amount = rawCustomScoreInput.toIntOrNull() ?: 1
                                    val finalVal = if (applyIsNegative) -amount else amount
                                    onApplyScore(finalVal)
                                },
                                enabled = rawCustomScoreInput.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("score_entry_custom_apply_button")
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WinnerCelebrationDialog(
    winnerName: String,
    onContinue: () -> Unit,
    onEndMatch: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Confetti particle canvas layer
                ConfettiEffect(modifier = Modifier.matchParentSize())

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFFEF9E7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFF1C40F),
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OFFICIAL MATCH WINNER!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFFD4AF37)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = winnerName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "The targets score ceiling has been reached! Show your victory card and decide the next fate:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    // Fate buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onEndMatch,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("winner_dialog_end_button")
                        ) {
                            Text("Save and End Match", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = onContinue,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("winner_dialog_continue_button")
                        ) {
                            Text("Keep Playing Match", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItalianFlagAccentBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillInputVerticalGradient(Color(0xFF008C45))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF4F9FF))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillInputVerticalGradient(Color(0xFFCD212A))
        )
    }
}

// Custom decoration modifier helpers
fun Modifier.fillInputVerticalGradient(color: Color): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(color, color.copy(alpha = 0.8f))
    )
)

private fun formatDuration(durationSeconds: Long): String {
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    return String.format("%02d:%02d", mins, secs)
}
