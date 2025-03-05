package com.example.sereno.features.chat.domain.model

import com.example.sereno.features.chat.data.model.Chat

data class ChatState(val chats: List<Chat> = emptyList(), val consumeWhole: Boolean = false) {
    fun merge(other: ChatState): ChatState {
        return copy(chats = chats + other.chats)
    }
}