package com.example.sereno.features.home.ui.view_model

import androidx.lifecycle.ViewModel
import com.example.sereno.features.home.domain.repo.HomeRepo
import com.example.sereno.features.home.domain.model.ArticleModel

class HomeViewModel : ViewModel() {
    private val homeRepo = HomeRepo()

    fun getArticles(): List<ArticleModel> {
        return homeRepo.getArticles()
    }
}