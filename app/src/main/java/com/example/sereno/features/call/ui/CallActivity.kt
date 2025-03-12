package com.example.sereno.features.call.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sereno.R
import com.example.sereno.audio.audio_manager.AudioSource
import com.example.sereno.audio.audio_manager.CallAudioManager
import com.example.sereno.common.extensions.hasMicPermission
import com.example.sereno.features.call.domain.CallViewModel
import com.example.sereno.features.call.domain.CallState
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityCallBinding
import com.example.sereno.features.call.domain.error.CallError
import com.example.sereno.features.call.domain.error.CallErrorActionType
import com.example.sereno.features.call.ui.animation.AnimationController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CallActivity : AppCompatActivity() {

    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding
    private val callAudioManager by lazy { CallAudioManager(this) }
    private val blinkAnimation = AnimationController(.3f, 1f, durationMs = 800)
    private val heartBeatAnimation = AnimationController(1f, 1.4f, durationMs = 640)
    private var timerJob: Job? = null
    private var elapsedTime: Long = 0
    private var startTime: Long = 0
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted(this)
        } else {
            viewModel.onPermissionDenied(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.init(this)
        requestMicPermissionIfRequired()
        setupWindowInsets()
        setupUI()
        observeViewModelState()
        viewModel.initCall(this)

    }

    private fun requestMicPermissionIfRequired() {
        if (!this.hasMicPermission()) requestMicPermission()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left, systemBars.top, systemBars.right, systemBars.bottom
            )
            insets
        }
    }

    private fun setupUI() {
        binding.apply {
            allowButton.icon.isVisible = false
            setupClickListeners()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            bottomCallActions.mute.onClickWithHaptics { viewModel.onBotSpeakingFinished() }
            icClose.onClickWithHaptics { finish() }
            bottomCallActions.endCall.onClickWithHaptics { finish() }
            tapToInterrupt.root.onClickWithHaptics { viewModel.onBotSpeakingFinished() }
        }
    }

    private fun requestMicPermission() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callState.collectLatest { state ->
                    handleCallState(state)
                }
            }
        }


    }

    private fun handleCallState(state: CallState) {
        stopAnimations()
        resetViewsOnStateChange()
        callAudioManager.stop()
        binding.errorContainer.isVisible = false
        when (state) {
            is CallState.Ringing -> setRingingState()
            is CallState.BotSpeaking -> handleBotSpeakingState(state)
            is CallState.UserSpeaking -> handleUserSpeakingState()
            is CallState.BotProcessing -> handleBotProcessingState()
            is CallState.Error -> handleError(state.error)
        }
    }

    private fun handleError(error: CallError) {
        binding.errorContainer.isVisible = true
        error.let {
            binding.title.text = it.title
            binding.description.text = it.description
            binding.allowButton.text.text = it.actionText
            binding.allowButton.root.onClickWithHaptics {
                when (it.actionType) {
                    CallErrorActionType.RETRY -> viewModel.initCall(this)
                    CallErrorActionType.SETTINGS -> {
                        startActivity(
                            Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.parse("package:$packageName")
                            )
                        )
                    }

                    CallErrorActionType.ALLOW -> requestMicPermission()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.callState.value is CallState.Error) {
            viewModel.initCall(this)
        }
    }

    private fun handleBotProcessingState() {
        setTalkingPersonBg(isBot = true)
        heartBeatAnimation.play(onProgress = {
            binding.talkingPerson.scaleX = it
            binding.talkingPerson.scaleY = it

        }, onRepeat = {
            binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        },
            onComplete = {
                resetTalkingPersonBgHearBeatSize()
            })
        binding.tapToInterrupt.root.isVisible = true

    }

    private fun handleUserSpeakingState() {
        setTalkingPersonBg(isBot = false)
        binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun handleBotSpeakingState(state: CallState.BotSpeaking) {
        binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        if (state.isFirstMessage) startTimer()
        callAudioManager.play(source = AudioSource.FileSource(state.audio),
            shouldLoop = false,
            onComplete = {
                viewModel.onBotSpeakingFinished()
            })
        binding.callingLoading.isVisible = false
        setTalkingPersonBg(isBot = true)
        binding.tapToInterrupt.root.isVisible = !state.isFirstMessage

    }

    private fun setRingingState() {
        callAudioManager.destroy()
        binding.talkingPerson.translationY = 0f
        callAudioManager.play(
            source = AudioSource.Resource(R.raw.ringing),
            shouldLoop = true,
        )
        blinkAnimation.play(onComplete = {
            binding.callingLoading.animate().alpha(1f).start()
        }, onProgress = {
            binding.callingLoading.alpha = it
        })
    }

    private fun stopAnimations() {
        blinkAnimation.stop()
        heartBeatAnimation.stop()
        resetTalkingPersonBgHearBeatSize()
    }

    private fun setTalkingPersonBg(isBot: Boolean) {
        val translateY = if (isBot) {
            -(binding.root.height * .38)
        } else {
            (binding.root.height * .38)
        }
        binding.talkingPerson.animate().translationY(translateY.toFloat()).start()
    }

    private fun resetViewsOnStateChange() {
        binding.tapToInterrupt.root.isVisible = false
    }

    private fun resetTalkingPersonBgHearBeatSize() {
        binding.root.animate().scaleX(1f).scaleY(1f).start()
    }

    private fun startTimer() {
        timerJob?.cancel()
        startTime = if (elapsedTime == 0L) {
            System.currentTimeMillis()
        } else {
            System.currentTimeMillis() - elapsedTime
        }

        timerJob = lifecycleScope.launch {
            while (isActive) {
                elapsedTime = System.currentTimeMillis() - startTime
                val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                binding.header.timer.text = timeFormatted
                delay(1000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        callAudioManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        callAudioManager.destroy()
        blinkAnimation.clear()
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            context.startActivity(intent)
        }
    }
}