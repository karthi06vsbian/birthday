package com.gaming.companion.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSession): Long

    @Query("SELECT * FROM training_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<TrainingSession>>

    @Query("SELECT MAX(accuracyPercentage) FROM training_sessions")
    fun getBestAccuracy(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM training_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("DELETE FROM training_sessions")
    suspend fun clearHistory()
}
