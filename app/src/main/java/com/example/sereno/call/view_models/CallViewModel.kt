package com.example.sereno.call.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.call.utils.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallViewModel : ViewModel() {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer()
    private val _currentState = MutableLiveData<CallState>(CallState.RINGING)

    companion object {
        private const val INITIAL_CALL_DELAY = 10000L
    }

    val currentState: LiveData<CallState> = _currentState

    fun call() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.RINGING
            }

            delay(INITIAL_CALL_DELAY)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.BotSpeaking(true)
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.UserSpeaking
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.BotSpeaking(false)
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.UserSpeaking
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.BotSpeaking(false)
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.UserSpeaking
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.BotSpeaking(false)
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.UserSpeaking
            }
            delay(4000)
            withContext(Dispatchers.Main) {
                _currentState.value = CallState.BotSpeaking(false)
            }
        }
    }
}

sealed class CallState {
    data object RINGING : CallState()
    data class BotSpeaking(val isFirstMessage: Boolean) : CallState()
    data object UserSpeaking : CallState()
}