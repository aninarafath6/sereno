package com.example.sereno.features.call_.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {

    private val _callState = MutableStateFlow<CallState>(CallState.Ringing)
    val callState = _callState.asStateFlow()

    private val _error = MutableStateFlow<CallError?>(null)
    val error = _error.asStateFlow()

    private fun initCall() {
        viewModelScope.launch {
            _callState.value = CallState.Ringing
        }
    }

    fun onBotSpeakingFinished() {
        viewModelScope.launch {
            _callState.value = CallState.UserSpeaking
        }
    }

    fun onPermissionGranted() {
        _error.value = null
        initCall()
    }

    fun onPermissionDenied(shouldOpenSettings: Boolean) {
        _error.value = CallError(
            title = "Microphone Permission Required",
            description = "Please grant microphone access to continue",
            actionText = if (shouldOpenSettings) "Settings" else "Allow"
        )
    }
}

sealed class CallState {
    data object Ringing : CallState()
    data object BotProcessing : CallState()
    data object UserSpeaking : CallState()
    data class BotSpeaking(
        val audio: String,
        val isFirstMessage: Boolean = false
    ) : CallState()
}

data class CallError(
    val title: String,
    val description: String,
    val actionText: String
)