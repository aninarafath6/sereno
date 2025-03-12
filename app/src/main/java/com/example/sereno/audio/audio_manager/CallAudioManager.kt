package com.example.sereno.audio.audio_manager

import android.content.Context
import android.media.MediaPlayer
import java.io.File

class CallAudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(
        source: AudioSource,
        shouldLoop: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        mediaPlayer?.release()
        when (source) {
            is AudioSource.FileSource -> {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(source.file.path)
                mediaPlayer?.prepare()
            }

            is AudioSource.Resource -> {
                mediaPlayer = MediaPlayer.create(context, source.resId)
            }
        }
        mediaPlayer?.apply {
            isLooping = shouldLoop
            start()
            setOnCompletionListener { onComplete?.invoke() }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun destroy() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

sealed class AudioSource {
    data class Resource(val resId: Int) : AudioSource()
    data class FileSource(val file: File) : AudioSource()
}