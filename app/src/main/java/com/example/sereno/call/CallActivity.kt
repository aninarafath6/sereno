package com.example.sereno.call

import AudioManager
import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.sereno.R
import com.example.sereno.call.utils.SpeechRecognizer
import com.example.sereno.call.view_models.CallState
import com.example.sereno.call.view_models.CallViewModel
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
        } else {
            binding.permissionRational.isVisible = false
        }
        vm.call()
        binding.root.post {
            binding.talkingPerson.translationY = getThirtyFivePercentOfScreenHeight()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SpeechRecognizer.hasMicPermission(this)) {
            binding.permissionRational.isVisible = false
        }
    }

    private fun setClickListeners() {
        binding.allowButton.root.onClickWithHaptics {
            if (shouldShowSettingsRationale) {
                openPermissionSettings()
            } else {
                requestMicPermission()
            }
        }

        binding.icClose.onClickWithHaptics {
            finish()
        }
        binding.bottomCallActions.endCall.onClickWithHaptics {
            finish()
        }
    }

    private fun initObservers() {
        vm.currentState.observe(this) { state ->
            AudioManager.toggleMute(this, shouldMute = true, shouldFade = false)
            when (state) {
                is CallState.RINGING -> setRingingState()
                is CallState.BotSpeaking -> handleBotSpeakingState(state.isFirstMessage)
                is CallState.UserSpeaking -> handleUserSpeakingState()
            }
        }
    }

    private fun requestMicPermission() {
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun setRingingState() {
        AudioManager.init(this, R.raw.ringing)
        AudioManager.toggleMute(this, shouldMute = false, shouldFade = false)

        binding.apply {
            callingLoading.isVisible = true
            tapToInterrupt.root.isVisible = false
            bottomCallActions.muteContainer.isVisible = false
            bottomCallActions.spacer.isVisible = false
            root.performHapticFeedback()
        }

        callingLoadingAlphaAnimation.start()
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

    private fun handleBotSpeakingState(isFirstMessage: Boolean) {
        binding.apply {
            root.performHapticFeedback()
            bottomCallActions.muteContainer.isVisible = true
            callingLoading.isVisible = false
            bottomCallActions.spacer.isVisible = true
            tapToInterrupt.root.isVisible = !isFirstMessage
        }

        if (isFirstMessage) startTimer()
        animateTalkingPerson(upwards = false)
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
        binding.permissionRational.isVisible = !granted
        if (granted) {
            onPermissionGranted()
            return@registerForActivityResult
        }

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
        // TODO: fire event
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