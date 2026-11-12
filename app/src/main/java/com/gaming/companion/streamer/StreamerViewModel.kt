package com.gaming.companion.streamer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StreamerUiState(
    val isPrivacyModeEnabled: Boolean = false,
    val flagSecureApplied: Boolean = false,
    val streamingSetup: String = "DUAL-DEVICE / OBS WORKFLOW",
    val statusMessage: String = "Companion window visible to screen recorders."
)

class StreamerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StreamerUiState())
    val uiState: StateFlow<StreamerUiState> = _uiState.asStateFlow()

    // Callback that MainActivity can register to toggle FLAG_SECURE on the Window
    var onFlagSecureToggled: ((Boolean) -> Unit)? = null

    fun togglePrivacyMode() {
        val newState = !_uiState.value.isPrivacyModeEnabled
        _uiState.value = _uiState.value.copy(
            isPrivacyModeEnabled = newState,
            flagSecureApplied = newState,
            statusMessage = if (newState) {
                "PROTECTED: Android FLAG_SECURE active. Companion window blocks recording & screenshots."
            } else {
                "UNPROTECTED: Companion window visible to screen recorders."
            }
        )
        onFlagSecureToggled?.invoke(newState)
    }
}
