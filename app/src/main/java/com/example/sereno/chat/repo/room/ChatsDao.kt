package com.example.sereno.chat.repo.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.repo.room.ChatsDatabase.Companion.CHATS_TABLE

@Dao
interface ChatsDao {
    @Insert
    suspend fun saveChat(chat: Chat)

    @Query("SELECT * FROM $CHATS_TABLE")
    fun getChats(): List<Chat>
}