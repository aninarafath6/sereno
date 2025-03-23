package com.example.sereno.core.settings

import android.content.Context
import android.content.SharedPreferences


object AppSettings {
    private lateinit var sharedPref: SharedPreferences
    private const val PREF_NAME = "better_co_pref"
    private const val PREF_USER_PREFER_MUTE = "user_prefer_mute"

    fun init(context: Context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setUserPreferMute(preferMute: Boolean) {
        sharedPref.edit().putBoolean(PREF_USER_PREFER_MUTE, preferMute).apply()
    }

    fun getUserMuteStatePreference(): Boolean {
        return sharedPref.getBoolean(PREF_USER_PREFER_MUTE, true)
    }
}