package com.example.sereno.features.home.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.features.home.domain.model.BaseAudio
import com.example.sereno.features.home.domain.model.CustomAudio
import com.example.sereno.features.home.domain.model.NormalAudio
import com.example.sereno.features.home.domain.repo.AudioRepo
import com.example.sereno.features.home.domain.use_cases.PersistAudioVolumeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioViewmodel : ViewModel() {
    private val audioRepo = AudioRepo()
    private var _resolvedAudios = MutableStateFlow<List<BaseAudio>>(emptyList())
    private val persistAudioVolumeUseCase = PersistAudioVolumeUseCase(audioRepo)
    private val isLoading = MutableLiveData(false)

    val getLoading: LiveData<Boolean> = isLoading
    val audios: StateFlow<List<BaseAudio>> get() = _resolvedAudios

    fun fetchFocusAudios() {
        viewModelScope.launch {
            isLoading.value = true
            _resolvedAudios.value = audioRepo.getNormalAudio()
            isLoading.value = false
        }
    }

    fun fetchSleepAudios() {
        viewModelScope.launch {
            isLoading.value = true
            _resolvedAudios.value = audioRepo.getNormalAudio()
            isLoading.value = false
        }
    }

    fun fetchCustomAudios() {
        viewModelScope.launch {
            isLoading.value = true
            _resolvedAudios.value = audioRepo.getCustomAudio()
            isLoading.value = false
        }
    }

    fun updateVolume(newAudio: CustomAudio) {
        updateItem(newAudio)
        persistAudioVolumeUseCase.execute(newAudio.id, newAudio.volumeLevel)
    }

    fun updatePlayingForCustomAudios(newAudio: CustomAudio) {
        updateItem(newAudio)
    }

    fun updatePlayingForNormalAudio(id: Long) {
        viewModelScope.launch {
            _resolvedAudios.value.map { audio ->
                audio as NormalAudio
                if (audio.id == id) {
                    audio.copy(isPlaying = !audio.isPlaying)
                } else {
                    audio.copy(isPlaying = false)
                }
            }.let {
                _resolvedAudios.emit(it)
            }
        }
    }

    private fun updateItem(newAudio: BaseAudio) {
        _resolvedAudios.value.map { audio ->
            if (audio.id == newAudio.id) newAudio else audio
        }.let {
            _resolvedAudios.value = it
        }
    }
}