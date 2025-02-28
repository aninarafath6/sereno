package com.example.sereno.chat.events

sealed interface ChatEvent {
    data class SwipeToReplay(val position: Int) : ChatEvent
    data class SendMessage(val message: String) : ChatEvent
    data class BotResponded(val messages: List<String>, val replayChat: String? = null) : ChatEvent
    data object LoadChats : ChatEvent
}