package com.example.sereno.audio.audio_manager

import android.content.Context

class CallAudioManager(private val context: Context) {
    private var currentAudioSource: AudioSource? = null
    private var isMuted = false

    fun play(
        source: AudioSource,
        shouldLoop: Boolean = false,
        shouldFade: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        currentAudioSource = source
    }

    fun mute(shouldMute: Boolean) {
        isMuted = shouldMute
    }

    fun destroy() {
        currentAudioSource = null
    }
}

sealed class AudioSource {
    data class Resource(val resId: Int) : AudioSource()
    data class FileSource(val filePath: String) : AudioSource()
}