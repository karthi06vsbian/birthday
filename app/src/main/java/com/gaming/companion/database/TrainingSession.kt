package com.gaming.companion.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalShots: Int,
    val hits: Int,
    val headshots: Int,
    val neckshots: Int,
    val accuracyPercentage: Float,
    val rating: String,
    val durationSeconds: Long,
    val sensitivitySetting: Float
)
