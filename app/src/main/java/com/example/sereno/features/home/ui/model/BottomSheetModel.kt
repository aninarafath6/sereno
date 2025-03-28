package com.example.sereno.features.home.ui.model

import com.example.sereno.features.home.ui.view_model.AudioViewmodel


data class BottomSheetModel(
    val audioVm: AudioViewmodel,
    val title: String,
    val subtitle: String,
    val getAudios: () -> Unit
)
