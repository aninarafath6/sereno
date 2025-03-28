package com.example.sereno.features.home.domain.model

abstract class BaseAudio {
    abstract val id: Long
    abstract val title: String
    abstract val musicURL: String
    abstract val previewImageUrl: String
    abstract val isPlaying: Boolean
}

data class NormalAudio(
    val subtitle: String,
    override val id: Long,
    override val title: String,
    override val musicURL: String,
    override val previewImageUrl: String,
    override val isPlaying: Boolean,
) : BaseAudio()

data class CustomAudio(
    val volumeLevel: Int,
    override val id: Long,
    override val title: String,
    override val musicURL: String,
    override val previewImageUrl: String,
    override val isPlaying: Boolean
) : BaseAudio()
