package com.example.sereno.chat.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.sereno.chat.repo.room.ChatsDatabase.Companion.CHATS_TABLE
import java.util.UUID

@Entity(tableName = CHATS_TABLE)
data class Chat(
    val message: String,
    val isBot: Boolean,
    val isNewChat: Boolean = true,
    val isSynced: Boolean = false,
    val replayChatId: String? = null,
    val createdAt: Long,
    val isContextRelevant: Boolean = true,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
)