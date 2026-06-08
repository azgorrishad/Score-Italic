package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.ScoreRepository
import com.example.data.model.GameMatch
import com.example.data.model.Round
import com.example.ui.util.SoundFeedbackHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScoreRepository
    val soundFeedbackHelper: SoundFeedbackHelper

    init {
        val database = AppDatabase.getDatabase(application)
        val prefs = PreferencesManager(application)
        repository = ScoreRepository(database.matchDao(), prefs)
        soundFeedbackHelper = SoundFeedbackHelper(application, repository)
    }

    // Setting Flows
    val currentTheme: StateFlow<PreferencesManager.AppTheme> = repository.theme
    val soundEnabled: StateFlow<Boolean> = repository.soundEnabled
    val hapticEnabled: StateFlow<Boolean> = repository.hapticEnabled

    // Historical Matches from Database
    val savedMatches: StateFlow<List<GameMatch>> = repository.allMatches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Statistics UI Cache Flow
    val statisticsState: StateFlow<StatisticsState> = savedMatches
        .combine(MutableStateFlow(Unit)) { matches, _ ->
            calculateStatistics(matches)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsState()
        )

    // Active Match State Holder
    data class ActiveMatch(
        val isMatchStarted: Boolean = false,
        val teamAName: String = "Team A",
        val teamBName: String = "Team B",
        val teamAScore: Int = 0,
        val teamBScore: Int = 0,
        val targetScore: Int = 40,
        val accumulatedDurationMs: Long = 0L,
        val lastTimerStartTimeMs: Long? = null,
        val rounds: List<Round> = emptyList(),
        val winnerName: String? = null,
        val winnerAcknowledged: Boolean = false
    ) {
        val streakTeamA: Int
            get() = getStreakForTeam(teamAName)

        val streakTeamB: Int
            get() = getStreakForTeam(teamBName)

        fun getCurrentDurationSeconds(now: Long = System.currentTimeMillis()): Long {
            var totalMs = accumulatedDurationMs
            if (lastTimerStartTimeMs != null) {
                totalMs += (now - lastTimerStartTimeMs)
            }
            return totalMs / 1000L
        }

        private fun getStreakForTeam(teamName: String): Int {
            var count = 0
            for (r in rounds.asReversed()) {
                if (r.scoreChange > 0 && r.teamName == teamName) {
                    count++
                } else if (r.scoreChange != 0) {
                    break
                }
            }
            return if (count >= 3) count else 0
        }
    }

    private val _activeMatch = MutableStateFlow(ActiveMatch())
    val activeMatch: StateFlow<ActiveMatch> = _activeMatch.asStateFlow()

    fun playSwipeSound() {
        soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.SWIPE)
    }

    fun playButtonClick() {
        soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.BUTTON_CLICK)
    }

    fun setTheme(theme: PreferencesManager.AppTheme) {
        repository.setTheme(theme)
    }

    fun setSoundEnabled(enabled: Boolean) {
        repository.setSoundEnabled(enabled)
    }

    fun setHapticEnabled(enabled: Boolean) {
        repository.setHapticEnabled(enabled)
    }

    fun clearAllHistory() {
        soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.ALERT)
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllMatches()
        }
    }

    // Match Cycle Functions
    fun startNewMatch(teamA: String, teamB: String, target: Int) {
        soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.START_MATCH)
        _activeMatch.value = ActiveMatch(
            isMatchStarted = true,
            teamAName = teamA.ifBlank { "Team A" },
            teamBName = teamB.ifBlank { "Team B" },
            teamAScore = 0,
            teamBScore = 0,
            targetScore = if (target > 0) target else 40,
            accumulatedDurationMs = 0L,
            lastTimerStartTimeMs = System.currentTimeMillis(),
            rounds = emptyList(),
            winnerName = null,
            winnerAcknowledged = false
        )
    }

    fun addRoundScore(teamName: String, scoreChange: Int) {
        val currentState = _activeMatch.value
        if (!currentState.isMatchStarted) return

        val nextRoundNumber = currentState.rounds.size + 1
        val newRound = Round(
            roundNumber = nextRoundNumber,
            teamName = teamName,
            scoreChange = scoreChange,
            timestamp = System.currentTimeMillis()
        )

        val updatedRounds = currentState.rounds + newRound
        val updatedAScore = if (teamName == currentState.teamAName) currentState.teamAScore + scoreChange else currentState.teamAScore
        val updatedBScore = if (teamName == currentState.teamBName) currentState.teamBScore + scoreChange else currentState.teamBScore

        // Check Winner condition (reaches or exceeds targetScore)
        var newWinner: String? = null
        if (!currentState.winnerAcknowledged) {
            if (updatedAScore >= currentState.targetScore && updatedAScore > updatedBScore) {
                newWinner = currentState.teamAName
            } else if (updatedBScore >= currentState.targetScore && updatedBScore > updatedAScore) {
                newWinner = currentState.teamBName
            }
        }

        // Calculate consecutive positive rounds for this team
        var streakCount = 0
        for (r in updatedRounds.asReversed()) {
            if (r.scoreChange > 0 && r.teamName == teamName) {
                streakCount++
            } else if (r.scoreChange != 0) {
                break
            }
        }
        val isStreak = streakCount >= 3 && scoreChange > 0

        if (newWinner != null) {
            stopTimer()
            soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.WINNER)
        } else if (isStreak) {
            soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.STREAK)
        } else {
            soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.SCORE_ADDED)
        }

        _activeMatch.update { current ->
            current.copy(
                teamAScore = updatedAScore,
                teamBScore = updatedBScore,
                rounds = updatedRounds,
                winnerName = newWinner ?: current.winnerName
            )
        }
    }

    fun undoLastRound() {
        val currentState = _activeMatch.value
        if (currentState.rounds.isEmpty()) return

        val lastRound = currentState.rounds.last()
        val updatedRounds = currentState.rounds.dropLast(1)

        val updatedAScore = if (lastRound.teamName == currentState.teamAName) {
            currentState.teamAScore - lastRound.scoreChange
        } else {
            currentState.teamAScore
        }

        val updatedBScore = if (lastRound.teamName == currentState.teamBName) {
            currentState.teamBScore - lastRound.scoreChange
        } else {
            currentState.teamBScore
        }

        soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.UNDO)

        // Recalculate winner state during undo
        var restoredWinner: String? = null
        if (currentState.winnerName != null && !currentState.winnerAcknowledged) {
            // If we undo out of a victory, we should clear the winner and resume timer
            restoredWinner = null
            startTimer()
        }

        _activeMatch.update { current ->
            current.copy(
                teamAScore = updatedAScore,
                teamBScore = updatedBScore,
                rounds = updatedRounds,
                winnerName = restoredWinner
            )
        }
    }

    fun continueMatchAfterWinner() {
        _activeMatch.update { current ->
            current.copy(
                winnerName = null,
                winnerAcknowledged = true
            )
        }
        startTimer()
    }

    fun endAndSaveMatch() {
        val state = _activeMatch.value
        if (!state.isMatchStarted) return

        val resolvedWinner = when {
            state.teamAScore > state.teamBScore -> state.teamAName
            state.teamBScore > state.teamAScore -> state.teamBName
            else -> "Draw"
        }

        val matchToSave = GameMatch(
            teamAName = state.teamAName,
            teamBName = state.teamBName,
            teamAScore = state.teamAScore,
            teamBScore = state.teamBScore,
            targetScore = state.targetScore,
            durationSeconds = state.getCurrentDurationSeconds(),
            timestamp = System.currentTimeMillis(),
            winnerName = resolvedWinner,
            rounds = state.rounds
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMatch(matchToSave)
        }

        resetActiveMatch()
    }

    fun resetActiveMatch() {
        soundFeedbackHelper.playSound(SoundFeedbackHelper.SoundType.RESET_MATCH)
        stopTimer()
        _activeMatch.value = ActiveMatch()
    }

    fun deleteMatchFromHistory(match: GameMatch) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMatch(match)
        }
    }

    // Timer Controls
    private fun startTimer() {
        _activeMatch.update { current ->
            if (current.lastTimerStartTimeMs == null && current.isMatchStarted) {
                current.copy(lastTimerStartTimeMs = System.currentTimeMillis())
            } else {
                current
            }
        }
    }

    private fun stopTimer() {
        _activeMatch.update { current ->
            if (current.lastTimerStartTimeMs != null) {
                val addedDuration = System.currentTimeMillis() - current.lastTimerStartTimeMs
                current.copy(
                    accumulatedDurationMs = current.accumulatedDurationMs + addedDuration,
                    lastTimerStartTimeMs = null
                )
            } else {
                current
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        soundFeedbackHelper.release()
    }

    private fun calculateStatistics(matches: List<GameMatch>): StatisticsState {
        if (matches.isEmpty()) return StatisticsState()

        val totalMatches = matches.size
        var winsA = 0
        var winsB = 0
        var highestScore = 0
        var totalDuration = 0L

        for (m in matches) {
            val resolvedWinner = m.winnerName
            if (resolvedWinner == m.teamAName) {
                winsA++
            } else if (resolvedWinner == m.teamBName) {
                winsB++
            }

            if (m.teamAScore > highestScore) highestScore = m.teamAScore
            if (m.teamBScore > highestScore) highestScore = m.teamBScore

            totalDuration += m.durationSeconds
        }

        val avgDuration = totalDuration / totalMatches

        return StatisticsState(
            totalMatches = totalMatches,
            winsTeamA = winsA,
            winsTeamB = winsB,
            winPercentageTeamA = if (totalMatches > 0) (winsA.toFloat() / totalMatches * 100) else 0f,
            winPercentageTeamB = if (totalMatches > 0) (winsB.toFloat() / totalMatches * 100) else 0f,
            highestScore = highestScore,
            averageDurationSeconds = avgDuration
        )
    }
}

data class StatisticsState(
    val totalMatches: Int = 0,
    val winsTeamA: Int = 0,
    val winsTeamB: Int = 0,
    val winPercentageTeamA: Float = 0f,
    val winPercentageTeamB: Float = 0f,
    val highestScore: Int = 0,
    val averageDurationSeconds: Long = 0L
)
