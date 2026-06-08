package com.example.ui.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import com.example.data.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundFeedbackHelper(
    context: Context,
    private val repository: ScoreRepository
) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    enum class SoundType {
        SCORE_ADDED,
        UNDO,
        WINNER,
        STREAK,
        BUTTON_CLICK,
        START_MATCH,
        RESET_MATCH,
        ALERT,
        SWIPE
    }

    fun playSound(type: SoundType) {
        if (!repository.soundEnabled.value) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (type) {
                    SoundType.SCORE_ADDED -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                    }
                    SoundType.UNDO -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                    }
                    SoundType.WINNER -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        Thread.sleep(150)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 450)
                    }
                    SoundType.STREAK -> {
                        // High-energy triple beep sequence
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
                        Thread.sleep(100)
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
                        Thread.sleep(100)
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                    }
                    SoundType.BUTTON_CLICK -> {
                        // Very short snappy click
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 40)
                    }
                    SoundType.START_MATCH -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 100)
                        Thread.sleep(150)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 150)
                    }
                    SoundType.RESET_MATCH -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)
                    }
                    SoundType.ALERT -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
                    }
                    SoundType.SWIPE -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        toneGenerator?.release()
    }
}
