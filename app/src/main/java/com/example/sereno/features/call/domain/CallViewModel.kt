package com.example.sereno.features.call.domain

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.audio.speech_recognizer.SpeechRecognizer
import com.example.sereno.audio.speech_recognizer.SpeechRecognizerListener
import com.example.sereno.common.extensions.hasMicPermission
import com.example.sereno.common.extensions.isInternetAvailable
import com.example.sereno.features.call.data.repo.CallRepo
import com.example.sereno.features.call.data.repo.CallResponse
import com.example.sereno.features.call.domain.error.CallError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CallViewModel : ViewModel(), SpeechRecognizerListener {
    private lateinit var callRepo: CallRepo
    private val speechRecognizer = SpeechRecognizer()
    private val _callState = MutableStateFlow<CallState>(CallState.Ringing)
    private var botFetchingJob: Job? = null
    private var asrRetiedCount = 0

    companion object {
        private const val ASR_RETRY_THRESHOLD = 3
    }

    private val _error = MutableStateFlow<CallError?>(null)
    val error = _error.asStateFlow()
    val callState = _callState.asStateFlow()

    fun init(context: Context) {
        callRepo = CallRepo(context)
        speechRecognizer.init(context)
        speechRecognizer.setListener(this)
    }

    fun initCall(context: Activity) {
        _error.value = null
        if (!context.hasMicPermission()) {
            _error.value = CallError.MicrophonePermissionRequired(shouldShowRational(context))
            return
        }
        if (!context.isInternetAvailable()) {
            _error.value = CallError.NoNetwork
            return
        }
        viewModelScope.launch {
            val ringingJob = launch {
                _callState.value = CallState.Ringing
                delay(5000L)
            }

            launch {
                val response = withContext(Dispatchers.IO) { callRepo.performCall("Hello!") }
                ringingJob.join()
                handleCallResponse(response, isFirstMessage = true)
            }
        }
    }

    fun onBotSpeakingFinished() {
        viewModelScope.launch {
            botFetchingJob?.cancel()
            speechRecognizer.listen()
            _callState.value = CallState.UserSpeaking
        }
    }

    fun onPermissionGranted(context: Activity) {
        _error.value = null
        initCall(context)
    }

    fun onPermissionDenied(activity: Activity) {
        _error.value = CallError.MicrophonePermissionRequired(shouldShowRational(activity))
    }

    private suspend fun handleCallResponse(response: CallResponse, isFirstMessage: Boolean) {
        when (response) {
            is CallResponse.Failed -> {
                _error.value = response.error
            }

            is CallResponse.Success -> {
                withContext(Dispatchers.Main) {
                    _callState.value = CallState.BotSpeaking(response.audioFile, isFirstMessage)
                }
            }
        }
    }

    private fun userMessageReceived(text: String) {
        botFetchingJob?.cancel()
        botFetchingJob = viewModelScope.launch {
            _callState.value = CallState.BotProcessing
            val response = withContext(Dispatchers.IO) { callRepo.performCall(text) }
            ensureActive()
            handleCallResponse(response, isFirstMessage = false)
        }
    }

    override fun onSpeechRecognized(text: String) {
        userMessageReceived(text)
        asrRetiedCount = 0
    }

    override fun onSpeechError(error: String) {
        if (asrRetiedCount > ASR_RETRY_THRESHOLD) {
            userMessageReceived("system:user message is not clear, ask to repeat")
            return
        }
        asrRetiedCount++
        speechRecognizer.listen()
    }

    private fun shouldShowRational(context: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context, Manifest.permission.RECORD_AUDIO
        )
    }
}

sealed class CallState {
    data object Ringing : CallState()
    data object BotProcessing : CallState()
    data object UserSpeaking : CallState()
    data class BotSpeaking(
        val audio: File, val isFirstMessage: Boolean = false
    ) : CallState()
}