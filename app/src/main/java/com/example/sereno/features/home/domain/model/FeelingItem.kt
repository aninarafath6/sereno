package com.example.sereno.features.home.domain.model

data class FeelingItem(
    val text: String,
    val iconResId: Int,
    var isSelected: Boolean = false,
    val description: String? = null,
    val subDescription: String? = null,
    val whatMade: String? = null,
)
