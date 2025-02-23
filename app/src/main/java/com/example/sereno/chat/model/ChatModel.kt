package com.example.sereno.chat.model

data class ChatModel(val chat: String, val owner: ChatOwner, val dateEpoch: Long)

enum class ChatOwner {
    USER, BOT
}