package com.example.sereno.common.audio_manager

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.io.IOException

class AmbientAudioManager {
    private val isMute = MutableLiveData(false)
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val FADE_DURATION = 800L
        private const val FADE_STEPS = 30
        private const val MAX_VOLUME = 0.5f
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentFadeJob: Job? = null

    fun play(
        context: Context,
        source: Int,
        shouldLoop: Boolean = false,
    ) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, source)
            mediaPlayer?.apply {
                isLooping = shouldLoop
                setVolume(0f, 0f)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun mute() {
        toggleMuteInternal(shouldMute = true)
    }

    fun unMute() {
        mediaPlayer?.start()
        toggleMuteInternal(shouldMute = false)
    }

    fun toggleMute() {
        if (isMute.value == true) unMute() else mute()
    }

    fun destroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentFadeJob?.cancel()
    }

    private fun toggleMuteInternal(shouldMute: Boolean) {
        isMute.value = shouldMute
        mediaPlayer?.let { player ->
            player.start()
            currentFadeJob?.cancel()

            if (shouldMute) {
                currentFadeJob = fadeVolume(MAX_VOLUME, 0f) {
                    player.pause()
                }
            } else {
                player.setVolume(0f, 0f)
                player.start()
                currentFadeJob = fadeVolume(0f, MAX_VOLUME)
            }
        }
    }

    fun getMuteStatus(): LiveData<Boolean> = isMute

    private fun fadeVolume(
        startVolume: Float,
        targetVolume: Float,
        onComplete: (() -> Unit)? = null
    ): Job {
        return scope.launch {
            val stepDelay = FADE_DURATION / FADE_STEPS
            val volumeStep = (targetVolume - startVolume) / FADE_STEPS

            for (i in 0..FADE_STEPS) {
                if (mediaPlayer == null) return@launch
                val newVolume = (startVolume + i * volumeStep).coerceIn(0f, MAX_VOLUME)
                mediaPlayer?.setVolume(newVolume, newVolume)
                delay(stepDelay)
            }
            onComplete?.invoke()
        }
    }
}