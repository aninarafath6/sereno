package com.example.sereno.features.chat.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sereno.features.chat.data.model.Chat
import com.example.sereno.features.chat.data.room.ChatsDatabase.Companion.CHATS_TABLE

@Dao
interface ChatsDao {
    @Insert
    suspend fun saveChat(chat: Chat)

    @Query("SELECT * FROM $CHATS_TABLE")
    fun getChats(): List<Chat>
}