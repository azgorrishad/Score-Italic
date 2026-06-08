package com.example.data

import com.example.data.model.GameMatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ScoreRepository(
    private val matchDao: MatchDao,
    private val preferencesManager: PreferencesManager
) {
    val allMatches: Flow<List<GameMatch>> = matchDao.getAllMatches()

    suspend fun insertMatch(match: GameMatch): Long {
        return matchDao.insertMatch(match)
    }

    suspend fun deleteMatch(match: GameMatch) {
        matchDao.deleteMatch(match)
    }

    suspend fun deleteAllMatches() {
        matchDao.deleteAllMatches()
    }

    val theme: StateFlow<PreferencesManager.AppTheme> = preferencesManager.theme
    val soundEnabled: StateFlow<Boolean> = preferencesManager.soundEnabled
    val hapticEnabled: StateFlow<Boolean> = preferencesManager.hapticEnabled

    fun setTheme(theme: PreferencesManager.AppTheme) {
        preferencesManager.setTheme(theme)
    }

    fun setSoundEnabled(enabled: Boolean) {
        preferencesManager.setSoundEnabled(enabled)
    }

    fun setHapticEnabled(enabled: Boolean) {
        preferencesManager.setHapticEnabled(enabled)
    }
}
