package com.example.sereno.common.di

import android.app.Application
import androidx.room.Room
import com.example.sereno.chat.repo.room.ChatsDao
import com.example.sereno.chat.repo.room.ChatsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton
    fun provideChatsDatabase(application: Application): ChatsDatabase {
        return Room.databaseBuilder(
            application,
            ChatsDatabase::class.java,
            ChatsDatabase.CHATS_DB
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideChatsDao(chatsDatabase: ChatsDatabase): ChatsDao {
        return chatsDatabase.dao
    }
}