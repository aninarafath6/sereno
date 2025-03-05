package com.example.sereno.features.call_.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sereno.features.call_.domain.CallError
import com.example.sereno.features.call_.domain.CallViewModel
import com.example.sereno.features.call_.domain.CallState
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityCallBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallActivity : AppCompatActivity() {

    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupUI()
        observeViewModelState()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
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
            allowButton.root.onClickWithHaptics { requestMicPermission() }
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collectLatest { error ->
                    handleErrorState(error)
                }
            }
        }
    }

    private fun handleCallState(state: CallState) {
        when (state) {
            is CallState.Ringing -> setRingingState()
            is CallState.BotSpeaking -> handleBotSpeakingState(state)
            is CallState.UserSpeaking -> handleUserSpeakingState()
            is CallState.BotProcessing -> handleBotProcessingState()
        }
    }

    private fun handleBotProcessingState() {

    }

    private fun handleUserSpeakingState() {

    }

    private fun handleBotSpeakingState(state: CallState.BotSpeaking) {

    }

    private fun setRingingState() {

    }

    private fun handleErrorState(error: CallError?) {
        binding.errorContainer.isVisible = error != null
        error?.let {
            binding.title.text = it.title
            binding.description.text = it.description
            binding.allowButton.text.text = it.actionText
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            context.startActivity(intent)
        }
    }
}