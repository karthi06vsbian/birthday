package com.gaming.companion.dashboard

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaming.companion.database.AppDatabase
import com.gaming.companion.database.TrainingSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val deviceModel: String = "OnePlus Device",
    val androidVersion: String = "Android 14 (OxygenOS)",
    val batteryLevel: Int = 88,
    val batteryTemperature: Float = 36.2f,
    val refreshRateHz: Int = 120,
    val totalTrainingSessions: Int = 0,
    val bestAccuracy: Float = 0f,
    val recentSession: TrainingSession? = null,
    val streamerPrivacyActive: Boolean = false,
    val radarDataActive: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val trainingDao = db.trainingDao()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        detectDeviceSpecs()
        observeDatabase()
        startTelemetryLoop()
    }

    private fun detectDeviceSpecs() {
        val model = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}"
        val version = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        _uiState.value = _uiState.value.copy(
            deviceModel = if (Build.MANUFACTURER.contains("oneplus", true)) model else "OnePlus Optimized ($model)",
            androidVersion = version
        )
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            trainingDao.getTotalSessionsCount().collect { count ->
                _uiState.value = _uiState.value.copy(totalTrainingSessions = count)
            }
        }

        viewModelScope.launch {
            trainingDao.getBestAccuracy().collect { best ->
                _uiState.value = _uiState.value.copy(bestAccuracy = best ?: 0f)
            }
        }

        viewModelScope.launch {
            trainingDao.getRecentSessions(1).collect { list ->
                _uiState.value = _uiState.value.copy(recentSession = list.firstOrNull())
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                readBatteryAndThermalTelemetry()
                delay(3000)
            }
        }
    }

    private fun readBatteryAndThermalTelemetry() {
        val context = getApplication<Application>()
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (batteryStatus != null) {
            val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85

            _uiState.value = _uiState.value.copy(
                batteryLevel = batteryPct,
                batteryTemperature = if (temp > 0) temp else 36.5f
            )
        }
    }
}
