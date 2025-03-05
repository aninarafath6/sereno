package com.example.sereno.common.audio_manager

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

object AudioManager {
    private val isMute = MutableLiveData(false)
    private var mediaPlayer: MediaPlayer? = null

    private const val FADE_DURATION = 800L
    private const val FADE_STEPS = 30
    private const val MAX_VOLUME = 0.5f

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentFadeJob: Job? = null

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
    }


    fun play(
        context: Context,
        source: AudioSource,
        shouldLoop: Boolean = false,
        shouldFade: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        try {
            if (mediaPlayer == null) {
                initMediaPlayer()
            } else {
                mediaPlayer?.reset()
            }

            mediaPlayer?.apply {
                when (source) {
                    is AudioSource.Resource -> {
                        mediaPlayer = MediaPlayer.create(context, source.resId)
                        mediaPlayer!!.start()
                        unMute(shouldFade)
                    }

                    is AudioSource.FileSource -> {
                        setDataSource(source.file.absolutePath)
                        prepare()
                    }
                }

                isLooping = shouldLoop
                setVolume(0f, 0f)
                setOnPreparedListener {
                    unMute(shouldFade)
                }
                setOnCompletionListener {
                    onComplete?.invoke()
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun mute(shouldFade: Boolean) {
        if (shouldFade) {
            toggleMuteInternal(shouldMute = true, shouldFade = shouldFade)
        } else {
            mediaPlayer?.pause()
        }
    }

    fun unMute(shouldFade: Boolean) {
        mediaPlayer?.start()
        toggleMuteInternal(shouldMute = false, shouldFade = shouldFade)
    }

    fun toggleMute(shouldFade: Boolean = false) {
        if (isMute.value == true) unMute(shouldFade) else mute(shouldFade)
    }

    fun destroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentFadeJob?.cancel()
    }

    private fun toggleMuteInternal(shouldMute: Boolean, shouldFade: Boolean = true) {
        isMute.value = shouldMute
        mediaPlayer?.let { player ->
            player.start()
            currentFadeJob?.cancel()

            if (shouldMute) {
                if (shouldFade) {
                    currentFadeJob = fadeVolume(MAX_VOLUME, 0f) {
                        player.pause()
                    }
                } else {
                    player.setVolume(0f, 0f)
                    player.pause()
                }
            } else {
                if (shouldFade) {
                    player.setVolume(0f, 0f)
                    player.start()
                    currentFadeJob = fadeVolume(0f, MAX_VOLUME)
                } else {
                    player.setVolume(MAX_VOLUME, MAX_VOLUME)
                    player.start()
                }
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

sealed class AudioSource {
    data class Resource(val resId: Int) : AudioSource()
    data class FileSource(val file: File) : AudioSource()
}