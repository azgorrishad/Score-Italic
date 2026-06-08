package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class GameMatch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamAName: String,
    val teamBName: String,
    val teamAScore: Int,
    val teamBScore: Int,
    val targetScore: Int,
    val durationSeconds: Long,
    val timestamp: Long,
    val winnerName: String,
    val rounds: List<Round>
)
