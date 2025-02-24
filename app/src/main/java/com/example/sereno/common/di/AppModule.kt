package com.example.sereno.common.di

import com.example.sereno.chat.repo.GroqRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    fun provideGroqRepo(): GroqRepo {
        return GroqRepo()
    }
}