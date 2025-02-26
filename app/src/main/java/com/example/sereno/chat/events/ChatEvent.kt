package com.example.sereno.chat.events

sealed interface ChatEvent {
    data class SendMessage(val message: String) : ChatEvent
    data class BotResponded(val message: String, val replayChat: String? = null) : ChatEvent
    data object LoadChats : ChatEvent
}