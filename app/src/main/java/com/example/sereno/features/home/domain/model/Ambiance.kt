package com.example.sereno.features.home.domain.model


sealed class Ambiance(
    val title: String,
    val subTitle: String,
) {
    data object Focus : Ambiance("Focus Flow", "Clarity, productivity, or concentration")
    data object Sleep : Ambiance("Sleep Soundscapes", "Calm, restful, or deep slumber")
    data object Custom :
        Ambiance("Custom sound mix", "Boost focus, relaxation, or sleep")
}