package com.example.sereno.features.home.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.features.home.domain.depo.HomeRepo
import com.example.sereno.features.home.domain.model.Ambiance
import com.example.sereno.features.home.domain.model.ArticleModel
import com.example.sereno.features.home.domain.model.AudioItem
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val homeRepo = HomeRepo()

    fun init() {

    }

    fun getAudios(
        ambiance: Ambiance,
        onLoading: () -> Unit,
        onAudiosReady: (audios: List<AudioItem>) -> Unit,
        onFailed: (e: String) -> Unit
    ) {
        viewModelScope.launch {
            onLoading()
            try {
                val audios = homeRepo.getMusicList(ambiance)
                onAudiosReady(audios)
            } catch (e: Exception) {
                onFailed("Something went wrong!")
            }
        }
    }

    fun getArticles(): List<ArticleModel> {
        return homeRepo.getArticles()
    }
}