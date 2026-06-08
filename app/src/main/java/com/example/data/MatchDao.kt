package com.example.data

import androidx.room.*
import com.example.data.model.GameMatch
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<GameMatch>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: Long): GameMatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: GameMatch): Long

    @Delete
    suspend fun deleteMatch(match: GameMatch)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()
}
