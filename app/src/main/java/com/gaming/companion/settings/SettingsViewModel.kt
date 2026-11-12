package com.gaming.companion.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaming.companion.database.AppDatabase
import com.gaming.companion.database.UserPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "Cyber Dark",
    val radarRefreshIntervalSeconds: Int = 3,
    val defaultSensitivity: Float = 1.0f,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val flagSecureDefault: Boolean = false,
    val historyClearedMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val prefDao = db.preferenceDao()
    private val trainingDao = db.trainingDao()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val radarSec = prefDao.getPreference("radar_interval")?.toIntOrNull() ?: 3
            val sens = prefDao.getPreference("default_sens")?.toFloatOrNull() ?: 1.0f
            val sfx = prefDao.getPreference("sound_effects")?.toBooleanStrictOrNull() ?: true
            val haptic = prefDao.getPreference("haptic")?.toBooleanStrictOrNull() ?: true

            _uiState.value = _uiState.value.copy(
                radarRefreshIntervalSeconds = radarSec,
                defaultSensitivity = sens,
                soundEffectsEnabled = sfx,
                hapticFeedbackEnabled = haptic
            )
        }
    }

    fun setRadarInterval(seconds: Int) {
        _uiState.value = _uiState.value.copy(radarRefreshIntervalSeconds = seconds)
        viewModelScope.launch {
            prefDao.setPreference(UserPreference("radar_interval", seconds.toString()))
        }
    }

    fun setDefaultSensitivity(value: Float) {
        _uiState.value = _uiState.value.copy(defaultSensitivity = value)
        viewModelScope.launch {
            prefDao.setPreference(UserPreference("default_sens", value.toString()))
        }
    }

    fun toggleSoundEffects(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(soundEffectsEnabled = enabled)
        viewModelScope.launch {
            prefDao.setPreference(UserPreference("sound_effects", enabled.toString()))
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hapticFeedbackEnabled = enabled)
        viewModelScope.launch {
            prefDao.setPreference(UserPreference("haptic", enabled.toString()))
        }
    }

    fun clearTrainingHistory() {
        viewModelScope.launch {
            trainingDao.clearHistory()
            _uiState.value = _uiState.value.copy(historyClearedMessage = "Training history reset successfully.")
        }
    }
}
