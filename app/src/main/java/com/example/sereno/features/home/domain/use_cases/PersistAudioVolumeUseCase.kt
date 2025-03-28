package com.example.sereno.features.home.domain.use_cases
import com.example.sereno.features.home.domain.repo.AudioRepo

class PersistAudioVolumeUseCase(private val musicRepo: AudioRepo) {
    fun execute(audioId: Long, volume: Int) {
        musicRepo.saveAudioVolume(audioId, volume)
    }
}