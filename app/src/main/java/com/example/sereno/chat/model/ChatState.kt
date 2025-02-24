package com.example.sereno.chat.model

data class ChatState(val chats: List<Chat> = emptyList(), val consumeWhole: Boolean = false)