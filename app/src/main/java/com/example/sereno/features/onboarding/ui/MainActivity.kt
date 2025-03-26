package com.example.sereno.features.onboarding.ui

import com.example.sereno.common.audio_manager.AmbientAudioManager
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.sereno.R
import com.example.sereno.common.extensions.isInternetAvailable
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityMainBinding
import com.example.sereno.features.home.ui.HomeActivity
import com.example.sereno.features.onboarding.domain.OnboardingViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private val vm by viewModels<OnboardingViewModel>()
    private val ambientAudioManager = AmbientAudioManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = resources.getColor(R.color.black)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        initUI()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun initUI() {
        setupInsets()
        vm.init(this)
        ambientAudioManager.play(
            this,
            source = R.raw.calm_ambient,
            shouldLoop = true,
        )
        initObservers()

        binding.volumeButton.muteButton.onClickWithHaptics {
            val isUserPreferMute = ambientAudioManager.toggleMute()
        }
        binding.login.onClickWithHaptics {
            if (!isInternetAvailable()) {
                Toast.makeText(
                    this,
                    "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@onClickWithHaptics
            }
            vm.loginWithGoogle(this, ::navigateToHome) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initObservers() {
        ambientAudioManager.getMuteStatus().observe(this) { isMuted ->
            val iconRes = if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on
            binding.volumeButton.ivMuteUnMute.setImageResource(iconRes)
        }
        vm.isLoading().observe(this) {
            binding.buttonContent.isVisible = !it
            binding.loading.isVisible = it
        }
    }

    override fun onPause() {
        ambientAudioManager.mute()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        ambientAudioManager.unMute()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        ambientAudioManager.destroy()
    }
}
