package com.example.sereno

import android.app.Application
import com.example.sereno.core.settings.AppSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SerenoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
    }
}