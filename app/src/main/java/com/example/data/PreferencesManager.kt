package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("score_italy_prefs", Context.MODE_PRIVATE)

    enum class AppTheme {
        MODERN_LIGHT,
        DARK,
        PURE_BLACK,
        GOLD_ELITE,
        ROYAL_BLUE,
        EMERALD_GREEN,
        CRIMSON_RED,
        PURPLE_NEON,
        CYBERPUNK,
        VINTAGE_CARD_TABLE
    }

    private val _theme = MutableStateFlow(getTheme())
    val theme: StateFlow<AppTheme> = _theme

    private val _soundEnabled = MutableStateFlow(isSoundEnabled())
    val soundEnabled: StateFlow<Boolean> = _soundEnabled

    private val _hapticEnabled = MutableStateFlow(isHapticEnabled())
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled

    fun getTheme(): AppTheme {
        val name = prefs.getString("theme", AppTheme.MODERN_LIGHT.name) ?: AppTheme.MODERN_LIGHT.name
        return try {
            AppTheme.valueOf(name)
        } catch (e: Exception) {
            AppTheme.MODERN_LIGHT
        }
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString("theme", theme.name).apply()
        _theme.value = theme
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("sound_enabled", true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun isHapticEnabled(): Boolean {
        return prefs.getBoolean("haptic_enabled", true)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
        _hapticEnabled.value = enabled
    }
}
