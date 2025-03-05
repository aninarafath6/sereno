package com.example.sereno.call.view_models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.call.repo.CallRepo
import com.example.sereno.call.repo.CallResponse
import com.example.sereno.audio.speech_recognizer.SpeechRecognizer
import com.example.sereno.audio.speech_recognizer.SpeechRecognizerListener
import com.example.sereno.common.extensions.isInternetAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CallViewModel : ViewModel(), SpeechRecognizerListener {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer()
    private val _currentState = MutableLiveData<CallState>(CallState.RINGING)
    private var _error = MutableLiveData<ErrorModel?>(null)
    private lateinit var callRepo: CallRepo
    private var isFirstBotMessage = true
    private var botResponseJob: Job? = null
    private var retryCount = 0
    private var context: Context? = null

    companion object {
        private const val INITIAL_CALL_DELAY = 5000L
    }

    val currentState: LiveData<CallState> = _currentState
    val error: LiveData<ErrorModel?> = _error

    fun init(context: Context) {
        this.context = context.applicationContext
        callRepo = CallRepo(context)
        speechRecognizer.init(context)
        speechRecognizer.setListener(this)
    }

    fun onResume(context: Context) {
        if (!context.isInternetAvailable()) {
            _error.value = ErrorModel(
                title = "No internet connection",
                description = "Please check your internet connection and try again",
                actionText = "Retry"
            )
        }
    }

    fun initCall() {
        viewModelScope.launch {
            val loading = launch(Dispatchers.Main) {
                _currentState.value = CallState.RINGING
                delay(INITIAL_CALL_DELAY)
            }
            fetchBotResponse("Hey", loading)
        }
    }

    fun onBotSpeakingFinished() {
        _currentState.value = CallState.UserSpeaking
        speechRecognizer.listen()
    }

    fun setPermissionGrandError() {
        _error.value = ErrorModel(
            title = "Allow mic permission",
            description = "Microphone access is required to join the therapy call session",
            actionText = "Allow"
        )
    }

    fun clearError() {
        _error.value = null
    }

    private fun handleBotResponse(response: CallResponse) {
        when (response) {
            is CallResponse.Failed -> {
                _error.value =
                    ErrorModel(
                        title = "Something went wrong",
                        description = response.message,
                        actionText = "Retry"
                    )
            }

            is CallResponse.Success -> {
                _error.value = null
                _currentState.value =
                    CallState.BotSpeaking(isFirstBotMessage, response.audioFile)
            }
        }
        isFirstBotMessage = false
    }

    override fun onSpeechRecognized(text: String) {
        Log.d("CallViewModel", "recognized: $text")

        _currentState.value = CallState.BotProcessing
        fetchBotResponse(text)
    }

    override fun onSpeechError(error: String) {
        if (retryCount < 3) {
            retryCount++
            speechRecognizer.listen()
        } else {
            retryCount = 0
            _currentState.value = CallState.BotProcessing
            fetchBotResponse("system: user message is not clear , request user to repeat.", null)
        }
    }


    private fun fetchBotResponse(text: String, initialLoadingJob: Job? = null) {
        if (context?.isInternetAvailable() == false) return
        botResponseJob?.cancel()
        botResponseJob = viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                callRepo.performCall(text)
            }
            initialLoadingJob?.join()
            ensureActive()
            handleBotResponse(response)
        }
    }
}

data class ErrorModel(val description: String, val actionText: String, val title: String)
sealed class CallState {
    data object RINGING : CallState()
    data class BotSpeaking(val isFirstMessage: Boolean, val audio: File) : CallState()
    data object BotProcessing : CallState()
    data object UserSpeaking : CallState()
}