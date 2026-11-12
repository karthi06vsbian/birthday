package com.gaming.companion.radar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaming.companion.nativeengine.NativeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class MarkerType {
    TRAINING_TARGET,
    TEAM_MATE,
    OBJECTIVE_DROP
}

data class TacticalMarker(
    val id: String,
    val label: String,
    val distanceMeters: Float,
    val bearingDegrees: Float,
    val type: MarkerType,
    val xOffsetPx: Float = 0f,
    val yOffsetPx: Float = 0f
)

data class RadarUiState(
    val sweepAngleDegrees: Float = 0f,
    val maxRangeMeters: Float = 100f,
    val refreshIntervalSeconds: Int = 3,
    val lastUpdatedTime: String = "--:--:--",
    val dataSourceStatus: String = "SIMULATED TACTICAL DATA (OFFLINE COMPANION)",
    val isAuthorizedApiConnected: Boolean = false,
    val markers: List<TacticalMarker> = emptyList(),
    val selectedMarker: TacticalMarker? = null
)

class RadarViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private var sweepJob: Job? = null
    private var refreshJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        startRadarSweep()
        startDataRefreshLoop()
    }

    fun setRefreshInterval(seconds: Int) {
        _uiState.value = _uiState.value.copy(refreshIntervalSeconds = seconds.coerceIn(1, 10))
        startDataRefreshLoop()
    }

    fun selectMarker(marker: TacticalMarker?) {
        _uiState.value = _uiState.value.copy(selectedMarker = marker)
    }

    private fun startRadarSweep() {
        sweepJob?.cancel()
        sweepJob = viewModelScope.launch {
            while (true) {
                delay(30) // ~33fps smooth radar sweep
                val newAngle = (_uiState.value.sweepAngleDegrees + 3f) % 360f
                _uiState.value = _uiState.value.copy(sweepAngleDegrees = newAngle)
            }
        }
    }

    private fun startDataRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                generateSimulatedTacticalData()
                val intervalMs = _uiState.value.refreshIntervalSeconds * 1000L
                delay(intervalMs)
            }
        }
    }

    private fun generateSimulatedTacticalData() {
        val maxRange = _uiState.value.maxRangeMeters
        val radarRadiusPx = 280f // virtual radius in canvas

        // Generate 4-6 tactical simulated training markers within 100 meters
        val newMarkers = mutableListOf<TacticalMarker>()

        val rawMarkers = listOf(
            Triple("TGT-A", MarkerType.TRAINING_TARGET, Random.nextFloat() * 35f + 15f), // 15 - 50m
            Triple("TGT-B", MarkerType.TRAINING_TARGET, Random.nextFloat() * 40f + 45f), // 45 - 85m
            Triple("ALLY-1", MarkerType.TEAM_MATE, Random.nextFloat() * 25f + 10f),      // 10 - 35m
            Triple("DROP-1", MarkerType.OBJECTIVE_DROP, Random.nextFloat() * 30f + 65f), // 65 - 95m
            Triple("TGT-C", MarkerType.TRAINING_TARGET, Random.nextFloat() * 20f + 75f)  // 75 - 95m
        )

        for ((label, type, dist) in rawMarkers) {
            val angle = Random.nextFloat() * 360f

            // Compute exact Cartesian offset using C++ JNI NativeEngine
            val (x, y) = NativeEngine.safeCalculateRadarPoint(
                angleDegrees = angle,
                distanceMeters = dist,
                maxRangeMeters = maxRange,
                radarRadiusPx = radarRadiusPx
            )

            newMarkers.add(
                TacticalMarker(
                    id = label,
                    label = label,
                    distanceMeters = dist,
                    bearingDegrees = angle,
                    type = type,
                    xOffsetPx = x,
                    yOffsetPx = y
                )
            )
        }

        _uiState.value = _uiState.value.copy(
            markers = newMarkers,
            lastUpdatedTime = timeFormat.format(Date())
        )
    }

    override fun onCleared() {
        super.onCleared()
        sweepJob?.cancel()
        refreshJob?.cancel()
    }
}
