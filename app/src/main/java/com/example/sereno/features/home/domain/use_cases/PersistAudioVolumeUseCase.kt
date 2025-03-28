package com.example.sereno.features.home.domain.use_cases
import com.example.sereno.features.home.domain.repo.MusicRepo

class PersistAudioVolumeUseCase(private val musicRepo: MusicRepo) {
    fun execute(audioId: Long, volume: Int) {
        musicRepo.saveAudioVolume(audioId, volume)
    }
}