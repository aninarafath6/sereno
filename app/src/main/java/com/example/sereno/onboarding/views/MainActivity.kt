package com.example.sereno.onboarding.views

import AmbientAudioManager
import android.content.Intent
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
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
import com.example.sereno.home.HomeActivity
import com.example.sereno.onboarding.view_model.OnboardingViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private val vm by viewModels<OnboardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        AmbientAudioManager.init(this)
        initAmbientVideo()
        initObservers()

        binding.volumeButton.muteButton.onClickWithHaptics {
            AmbientAudioManager.toggleMute(this)
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
                // Handle error
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
        AmbientAudioManager.getMuteStatus().observe(this) { isMuted ->
            val iconRes = if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on
            binding.volumeButton.ivMuteUnMute.setImageResource(iconRes)
        }
        vm.isLoading().observe(this){
            binding.buttonContent.isVisible = !it
            binding.loading.isVisible = it
        }
    }

    private fun initAmbientVideo() {
        mediaPlayer = MediaPlayer.create(
            this,
            Uri.parse("android.resource://$packageName/${R.raw.onbaording_bg_vd}")
        )
        binding.VideoView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mediaPlayer?.apply {
                    setSurface(Surface(surface))
                    isLooping = true
                    setVolume(0f, 0f)
                    setOnPreparedListener {
                        it.start()
                        adjustVideoSize(it, binding.VideoView)
                    }
                    start()
                }
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private fun adjustVideoSize(mediaPlayer: MediaPlayer, textureView: TextureView) {
        val videoRatio = mediaPlayer.videoWidth.toFloat() / mediaPlayer.videoHeight
        val screenRatio = textureView.width.toFloat() / textureView.height

        val scaleX = if (videoRatio > screenRatio) videoRatio / screenRatio else 1f
        val scaleY = if (videoRatio > screenRatio) 1f else screenRatio / videoRatio

        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, textureView.width / 2f, textureView.height / 2f)
        textureView.setTransform(matrix)
    }

    override fun onPause() {
        super.onPause()
        AmbientAudioManager.toggleMute(this, shouldMute = true)
    }

    override fun onResume() {
        super.onResume()
        AmbientAudioManager.toggleMute(this, shouldMute = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
