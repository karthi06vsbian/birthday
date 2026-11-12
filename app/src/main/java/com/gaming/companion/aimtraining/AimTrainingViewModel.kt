package com.gaming.companion.aimtraining

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaming.companion.database.AppDatabase
import com.gaming.companion.database.TrainingSession
import com.gaming.companion.nativeengine.NativeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AimTarget(
    val x: Float = 500f,
    val y: Float = 600f,
    val radius: Float = 85f,
    val velocityX: Float = 6f,
    val velocityY: Float = 4f
)

data class ShotFeedback(
    val x: Float,
    val y: Float,
    val zone: Int, // 0 = miss, 1 = body, 2 = neck, 3 = head
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AimTrainingUiState(
    val isSessionActive: Boolean = false,
    val remainingTimeSeconds: Int = 30,
    val totalSessionDuration: Long = 30L,
    val totalShots: Int = 0,
    val hits: Int = 0,
    val headshots: Int = 0,
    val neckshots: Int = 0,
    val accuracy: Float = 0f,
    val lastDragScore: Float = 0f,
    val sensitivity: Float = 1.0f,
    val currentRating: String = "READY",
    val target: AimTarget = AimTarget(),
    val crosshairX: Float = 500f,
    val crosshairY: Float = 800f,
    val recentFeedbacks: List<ShotFeedback> = emptyList(),
    val lastSavedSessionId: Long? = null
)

class AimTrainingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val trainingDao = db.trainingDao()

    private val _uiState = MutableStateFlow(AimTrainingUiState())
    val uiState: StateFlow<AimTrainingUiState> = _uiState.asStateFlow()

    private var targetLoopJob: Job? = null
    private var timerJob: Job? = null

    fun setSensitivity(value: Float) {
        _uiState.value = _uiState.value.copy(sensitivity = value.coerceIn(0.5f, 3.0f))
    }

    fun updateCrosshair(x: Float, y: Float) {
        _uiState.value = _uiState.value.copy(crosshairX = x, crosshairY = y)
    }

    fun startSession(durationSeconds: Int = 30) {
        if (_uiState.value.isSessionActive) return

        _uiState.value = _uiState.value.copy(
            isSessionActive = true,
            remainingTimeSeconds = durationSeconds,
            totalSessionDuration = durationSeconds.toLong(),
            totalShots = 0,
            hits = 0,
            headshots = 0,
            neckshots = 0,
            accuracy = 0f,
            lastDragScore = 0f,
            currentRating = "TRAINING...",
            recentFeedbacks = emptyList()
        )

        startTargetMovement()
        startTimer()
    }

    fun stopSession() {
        if (!_uiState.value.isSessionActive) return

        targetLoopJob?.cancel()
        timerJob?.cancel()

        val state = _uiState.value
        val rating = NativeEngine.safeEvaluateRating(
            hits = state.hits,
            totalShots = state.totalShots,
            headshots = state.headshots,
            neckshots = state.neckshots,
            durationSeconds = state.totalSessionDuration - state.remainingTimeSeconds
        )

        _uiState.value = state.copy(
            isSessionActive = false,
            currentRating = rating
        )

        // Save session to Room DB
        if (state.totalShots > 0) {
            viewModelScope.launch {
                val session = TrainingSession(
                    totalShots = state.totalShots,
                    hits = state.hits,
                    headshots = state.headshots,
                    neckshots = state.neckshots,
                    accuracyPercentage = state.accuracy,
                    rating = rating,
                    durationSeconds = state.totalSessionDuration - state.remainingTimeSeconds,
                    sensitivitySetting = state.sensitivity
                )
                val id = trainingDao.insertSession(session)
                _uiState.value = _uiState.value.copy(lastSavedSessionId = id)
            }
        }
    }

    fun processShot(tapX: Float, tapY: Float, dragStartX: Float? = null, dragStartY: Float? = null) {
        if (!_uiState.value.isSessionActive) return

        val state = _uiState.value
        val target = state.target

        // 1. Calculate hit zone via C++ JNI
        val zone = NativeEngine.safeCalculateHitZone(
            tapX = tapX,
            tapY = tapY,
            targetX = target.x,
            targetY = target.y,
            targetRadius = target.radius
        )

        // 2. Calculate drag score if swipe gesture occurred
        var dragScore = state.lastDragScore
        if (dragStartX != null && dragStartY != null) {
            dragScore = NativeEngine.calculateDragScore(
                startX = dragStartX,
                startY = dragStartY,
                endX = tapX,
                endY = tapY,
                targetHeadX = target.x,
                targetHeadY = target.y - (target.radius * 0.5f)
            )
        }

        val isHit = zone > 0
        val newTotalShots = state.totalShots + 1
        val newHits = if (isHit) state.hits + 1 else state.hits
        val newHeadshots = if (zone == 3) state.headshots + 1 else state.headshots
        val newNeckshots = if (zone == 2) state.neckshots + 1 else state.neckshots

        // 3. Compute accuracy via C++ JNI
        val accuracy = NativeEngine.safeCalculateAccuracy(newHits, newTotalShots)

        val feedbackText = when (zone) {
            3 -> "HEADSHOT! (+100)"
            2 -> "NECK HIT (+75)"
            1 -> "BODY HIT (+40)"
            else -> "MISS"
        }

        val newFeedback = ShotFeedback(tapX, tapY, zone, feedbackText)
        val feedbacks = (listOf(newFeedback) + state.recentFeedbacks).take(4)

        _uiState.value = state.copy(
            totalShots = newTotalShots,
            hits = newHits,
            headshots = newHeadshots,
            neckshots = newNeckshots,
            accuracy = accuracy,
            lastDragScore = dragScore,
            recentFeedbacks = feedbacks,
            crosshairX = tapX,
            crosshairY = tapY
        )

        // If target was hit, reposition target to simulate dynamic repositioning
        if (isHit) {
            relocateTarget()
        }
    }

    private fun relocateTarget() {
        val currentTarget = _uiState.value.target
        val newX = Random.nextFloat() * 600f + 150f
        val newY = Random.nextFloat() * 700f + 300f
        val speedX = (Random.nextFloat() * 8f + 4f) * (if (Random.nextBoolean()) 1 else -1)
        val speedY = (Random.nextFloat() * 6f + 3f) * (if (Random.nextBoolean()) 1 else -1)

        _uiState.value = _uiState.value.copy(
            target = currentTarget.copy(x = newX, y = newY, velocityX = speedX, velocityY = speedY)
        )
    }

    private fun startTargetMovement() {
        targetLoopJob?.cancel()
        targetLoopJob = viewModelScope.launch {
            while (_uiState.value.isSessionActive) {
                delay(16) // ~60fps target movement
                val current = _uiState.value.target
                val sens = _uiState.value.sensitivity

                var nx = current.x + (current.velocityX * sens)
                var ny = current.y + (current.velocityY * sens)
                var vx = current.velocityX
                var vy = current.velocityY

                // Screen boundaries bounce (approximate standard canvas dimensions)
                if (nx - current.radius < 50f || nx + current.radius > 950f) {
                    vx = -vx
                    nx = nx.coerceIn(50f + current.radius, 950f - current.radius)
                }
                if (ny - current.radius < 250f || ny + current.radius > 1150f) {
                    vy = -vy
                    ny = ny.coerceIn(250f + current.radius, 1150f - current.radius)
                }

                _uiState.value = _uiState.value.copy(
                    target = current.copy(x = nx, y = ny, velocityX = vx, velocityY = vy)
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTimeSeconds > 0 && _uiState.value.isSessionActive) {
                delay(1000)
                val newSeconds = _uiState.value.remainingTimeSeconds - 1
                _uiState.value = _uiState.value.copy(remainingTimeSeconds = newSeconds)
                if (newSeconds <= 0) {
                    stopSession()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        targetLoopJob?.cancel()
        timerJob?.cancel()
    }
}
