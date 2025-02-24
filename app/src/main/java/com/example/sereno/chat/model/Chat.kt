package com.example.sereno.chat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sereno.chat.repo.room.ChatsDatabase.Companion.CHATS_TABLE

@Entity(tableName = CHATS_TABLE)
data class Chat(
    val message: String,
    val isBot: Boolean,
    val isNewChat: Boolean = true,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val isContextRelevant: Boolean = true,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)