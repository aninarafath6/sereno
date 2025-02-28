package com.example.sereno.chat.model

data class ChatState(val chats: List<Chat> = emptyList(), val consumeWhole: Boolean = false) {
    fun merge(other: ChatState): ChatState {
        return copy(chats = chats + other.chats)
    }
}