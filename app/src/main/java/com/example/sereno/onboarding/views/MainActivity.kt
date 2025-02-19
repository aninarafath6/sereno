package com.example.sereno.onboarding.views

import AmbientAudioManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
        initObservers()
        initAmbientVideo()
    }

    private fun adjustVideoSize(mediaPlayer: MediaPlayer, textureView: TextureView) {
        val videoWidth = mediaPlayer.videoWidth
        val videoHeight = mediaPlayer.videoHeight
        val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()

        val screenWidth = textureView.width
        val screenHeight = textureView.height
        val screenRatio = screenWidth.toFloat() / screenHeight.toFloat()

        val scaleX: Float
        val scaleY: Float

        if (videoRatio > screenRatio) {
            // Video is wider than screen, fit height
            scaleX = videoRatio / screenRatio
            scaleY = 1f
        } else {
            // Video is taller than screen, fit width
            scaleY = screenRatio / videoRatio
            scaleX = 1f
        }

        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, screenWidth / 2f, screenHeight / 2f)
        textureView.setTransform(matrix)
    }


    private fun init() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AmbientAudioManager.init(this)
        mediaPlayer = MediaPlayer.create(
            this,
            Uri.parse("android.resource://" + packageName + "/" + R.raw.onbaording_bg_vd)
        )
        binding.volumeButton.muteButton.onClickWithHaptics {
            AmbientAudioManager.toggleMute(this)
        }
    }


    private fun initObservers() {
        AmbientAudioManager.getMuteStatus().observe(this) {
            if (it) {
                binding.volumeButton.ivMuteUnMute.setImageResource(R.drawable.ic_volume_off)
            } else {
                binding.volumeButton.ivMuteUnMute.setImageResource(R.drawable.ic_volume_on)
            }
        }
    }

    private fun initAmbientVideo() {
        binding.VideoView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mediaPlayer.setSurface(Surface(surface))

                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f)
                mediaPlayer.start()

                mediaPlayer.setOnPreparedListener {
                    it.start()
                    adjustVideoSize(it, binding.VideoView)
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
        mediaPlayer.release()
        AmbientAudioManager.release()
    }
}