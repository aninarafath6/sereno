package com.example.sereno.common.di

import android.content.Context
import androidx.room.Room
import com.example.sereno.audio.speech_recognizer.SpeechRecognizer
import com.example.sereno.features.chat.data.room.ChatsDao
import com.example.sereno.features.chat.data.room.ChatsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChatsDatabase(@ApplicationContext application: Context): ChatsDatabase {
        return Room.databaseBuilder(
            application,
            ChatsDatabase::class.java,
            ChatsDatabase.CHATS_DB
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatsDao(chatsDatabase: ChatsDatabase): ChatsDao {
        return chatsDatabase.dao
    }
    @Provides
    fun provideSpeechRecognizer(): SpeechRecognizer {
        return SpeechRecognizer()
    }
}