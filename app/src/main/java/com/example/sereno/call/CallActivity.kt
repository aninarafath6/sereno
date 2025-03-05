package com.example.sereno.call

import com.example.sereno.common.audio_manager.AudioManager
import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnRepeat
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.sereno.R
import com.example.sereno.call.utils.SpeechRecognizer
import com.example.sereno.call.view_models.CallState
import com.example.sereno.call.view_models.CallViewModel
import com.example.sereno.common.audio_manager.AudioSource
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityCallBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CallActivity : AppCompatActivity() {
    private val vm: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding
    private var shouldShowSettingsRationale: Boolean = false
    private var timerJob: Job? = null
    private var startTime = 0L
    private var elapsedTime = 0L
    private val callingLoadingAlphaAnimation = ValueAnimator.ofFloat(1f, 0.3f, 1f).apply {
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = LinearInterpolator()
        addUpdateListener {
            val alphaValue = it.animatedValue as Float
            binding.callingLoading.alpha = alphaValue
        }
    }
    private val botResponseLoadingAnimation = ValueAnimator.ofFloat(1f, 2f).apply {
        duration = 800
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animator ->
            val scaleValue = animator.animatedValue as Float
            binding.talkingPerson.scaleX = scaleValue
            binding.talkingPerson.scaleY = scaleValue
        }
        addListener {
            doOnCancel {
                binding.talkingPerson.scaleX = 1f
                binding.talkingPerson.scaleY = 1f
            }
            doOnRepeat {
                binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.allowButton.icon.isVisible = false
        setClickListeners()
        initObservers()
        if (!SpeechRecognizer.hasMicPermission(this)) {
            requestMicPermission()
        }

        vm.init(this)
        vm.initCall()
        binding.root.post {
            binding.talkingPerson.translationY = getThirtyFivePercentOfScreenHeight()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SpeechRecognizer.hasMicPermission(this)) {
            vm.clearError()
        }
        vm.onResume(this)
    }

    private fun setClickListeners() {
        binding.allowButton.root.onClickWithHaptics {
            if (shouldShowSettingsRationale) {
                openPermissionSettings()
            } else {
                requestMicPermission()
            }
        }
        binding.bottomCallActions.mute.onClickWithHaptics {
            vm.onBotSpeakingFinished()
        }

        binding.icClose.onClickWithHaptics {
            finish()
        }
        binding.bottomCallActions.endCall.onClickWithHaptics {
            finish()
        }
        binding.tapToInterrupt.root.onClickWithHaptics {
            vm.onBotSpeakingFinished()
        }
    }

    private fun initObservers() {
        vm.currentState.observe(this) { state ->
            AudioManager.mute(true)
            botResponseLoadingAnimation.cancel()
            botResponseLoadingAnimation
            when (state) {
                is CallState.RINGING -> setRingingState()
                is CallState.BotSpeaking -> handleBotSpeakingState(state)
                is CallState.UserSpeaking -> handleUserSpeakingState()
                CallState.BotProcessing -> {
                    animateTalkingPerson(upwards = false)
                    botResponseLoadingAnimation.start()
                    binding.tapToInterrupt.root.isVisible = true
                }
            }
        }
        vm.error.observe(this) {
            binding.errorContainer.isVisible = it != null
            binding.title.text = it?.title
            binding.description.text = it?.description
            binding.allowButton.text.text = it?.actionText
            binding.allowButton.icon.isVisible = false
            AudioManager.destroy()
        }
    }

    private fun requestMicPermission() {
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun setRingingState() {
        binding.apply {
            callingLoading.isVisible = true
            tapToInterrupt.root.isVisible = false
            bottomCallActions.muteContainer.isVisible = false
            bottomCallActions.spacer.isVisible = false
            root.performHapticFeedback()
        }

        callingLoadingAlphaAnimation.start()
        AudioManager.play(
            this, source = AudioSource.Resource(R.raw.ringing), shouldLoop = true, shouldFade = true
        )
    }

    private fun handleUserSpeakingState() {
        binding.apply {
            bottomCallActions.muteContainer.isVisible = true
            bottomCallActions.spacer.isVisible = true
            tapToInterrupt.root.isVisible = false
            callingLoading.isVisible = false
            root.performHapticFeedback()
        }
        animateTalkingPerson(upwards = true)
    }

    private fun handleBotSpeakingState(botSpeaking: CallState.BotSpeaking) {
        binding.apply {
            root.performHapticFeedback()
            bottomCallActions.muteContainer.isVisible = true
            callingLoading.isVisible = false
            bottomCallActions.spacer.isVisible = true
            tapToInterrupt.root.isVisible = !botSpeaking.isFirstMessage
        }

        if (botSpeaking.isFirstMessage) startTimer()
        animateTalkingPerson(upwards = false)
        AudioManager.play(
            this,
            source = AudioSource.FileSource(botSpeaking.audio),
            shouldLoop = false,
            shouldFade = false
        ) {
            vm.onBotSpeakingFinished()
        }
    }

    private fun View.performHapticFeedback() {
        this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun animateTalkingPerson(upwards: Boolean) {
        val offset = getThirtyFivePercentOfScreenHeight()
        binding.talkingPerson.animate().translationY(if (upwards) offset else -offset).start()
    }

    private fun openPermissionSettings() {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionGranted()
            vm.clearError()
            return@registerForActivityResult
        }
        vm.setPermissionGrandError()
        binding.allowButton.text.text = if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO
            )
        ) {
            shouldShowSettingsRationale = false
            "Allow"
        } else {
            shouldShowSettingsRationale = true
            "Settings"
        }
    }

    private fun onPermissionGranted() {
        // TODO: fire events
    }

    private fun getThirtyFivePercentOfScreenHeight(): Float {
        return (binding.root.height * 0.35).toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        callingLoadingAlphaAnimation.removeAllUpdateListeners()
        timerJob?.cancel()
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            context.startActivity(intent)
        }
    }
}