package com.example.sereno.features.home.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.features.home.domain.repo.MusicRepo
import com.example.sereno.features.home.domain.model.BaseAudio
import com.example.sereno.features.home.domain.model.CustomAudio
import com.example.sereno.features.home.domain.use_cases.PersistAudioVolumeUseCase
import kotlinx.coroutines.launch

class AudioViewmodel : ViewModel() {
    private val audioRepo = MusicRepo()
    private var _resolvedAudios = MutableLiveData<List<BaseAudio>>()
    private val persistAudioVolumeUseCase = PersistAudioVolumeUseCase(audioRepo)
    private val isLoading = MutableLiveData(false)

    val getLoading: LiveData<Boolean> = isLoading
    val audios: LiveData<List<BaseAudio>> get() = _resolvedAudios

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

    fun updateItem(newAudio: BaseAudio) {
        val currentList = _resolvedAudios.value ?: return
        val updatedList = currentList.map { audio ->
            if (audio.id == newAudio.id) {
                newAudio
            } else {
                audio
            }
        }
        _resolvedAudios.value = updatedList
    }
}