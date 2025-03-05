package com.example.sereno.features.chat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sereno.features.chat.data.room.ChatsDatabase.Companion.CHATS_TABLE
import java.util.UUID

@Entity(tableName = CHATS_TABLE)
data class Chat(
    val message: String,
    val isBot: Boolean,
    val isSynced: Boolean = false,
    val replayChatId: String? = null,
    val createdAt: Long,
    val isContextRelevant: Boolean = true,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
) {
    companion object {
        fun generateErrorChat(message: String, replayChatId: String?): Chat {
            return Chat(
                message,
                isBot = true,
                isContextRelevant = false,
                replayChatId = replayChatId,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}

fun Chat.isUser(): Boolean = !isBot
