package com.example.sereno.features.home.domain.model
import com.example.sereno.R

sealed class AmbientItem(val source: Int?) {
    data object DEFAULT : AmbientItem(R.raw.ambiant_music)
    data object FOCUS : AmbientItem(R.raw.focus_music)
    data object CUSTOM : AmbientItem(null)
    data object SLEEP : AmbientItem(R.raw.music_sleep)
}