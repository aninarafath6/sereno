package com.example.sereno.features.home.data

import android.content.Context
import android.content.SharedPreferences

object AudioSettingsManager {
    private lateinit var sharedPref: SharedPreferences

    private const val PREF_NAME = "audio_settings_preference"
    private const val PREF_USER_PREFERRED_VOLUME = "user_preferred_volume"

    fun init(context: Context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setAudioVolume(id: Long, volume: Int) {
        sharedPref.edit().putInt(PREF_USER_PREFERRED_VOLUME + id, volume).apply()
    }

    fun getAudioVolume(id: Long): Int? {
        val volume = sharedPref.getInt(PREF_USER_PREFERRED_VOLUME + id, -1)
        return volume.takeIf { it != -1 }
    }
}