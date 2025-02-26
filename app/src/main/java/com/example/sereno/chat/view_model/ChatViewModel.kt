package com.example.sereno.chat.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.chat.events.ChatEvent
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.ChatState
import com.example.sereno.chat.repo.ChatResponse
import com.example.sereno.chat.repo.GroqRepo
import com.example.sereno.chat.repo.room.ChatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val groqRepo: GroqRepo,
    private val dao: ChatsDao
) : ViewModel() {

    private val _chats = MutableStateFlow(ChatState())
    private val _isLoading = MutableLiveData(false)

    val chats: StateFlow<ChatState> = _chats
    val isLoading: LiveData<Boolean> = _isLoading

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.BotResponded -> handleBotResponse(event)
            ChatEvent.LoadChats -> loadChats()
            is ChatEvent.SendMessage -> sendMessage(event)
        }
    }

    private fun sendMessage(event: ChatEvent.SendMessage) {
        if (event.message.trim().isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val userChat = Chat(
                message = event.message.trim(),
                isBot = false,
                createdAt = System.currentTimeMillis()
            )
            saveAndUpdateChat(userChat) // show user chat and save it to db
            withContext(Dispatchers.IO) {
                val chatMessage = when (val chatResponse = groqRepo.chat(userChat.message)) {
                    is ChatResponse.Failed -> "Sorry, I'm not able to respond to that."
                    is ChatResponse.Success -> chatResponse.response
                }
                onEvent(ChatEvent.BotResponded(chatMessage))
            }
            _isLoading.value = false
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            val chats = withContext(Dispatchers.IO) { dao.getChats() }
            _chats.value = ChatState(chats, consumeWhole = true)

            if (chats.isEmpty()) {
                saveAndUpdateChat(NEW_BOT_CHAT)
            }
        }
    }

    private fun handleBotResponse(botResponse: ChatEvent.BotResponded) {
        viewModelScope.launch {
            val botChat = Chat(
                message = botResponse.message,
                isBot = true,
                createdAt = System.currentTimeMillis()
            )
            saveAndUpdateChat(botChat)
        }
    }

    private suspend fun saveAndUpdateChat(chat: Chat) {
        // Todo: Handle exceptions, like what if user has no enough space to store the chat, etc..
        withContext(Dispatchers.IO) {
            dao.saveChat(chat)
        }
        _chats.value = _chats.value.copy(chats = _chats.value.chats + chat, consumeWhole = false)
    }


    companion object {
        private val NEW_BOT_CHAT = Chat(
            message = "Hello, I'm Sereno. How can I help you?",
            isBot = true,
            createdAt = System.currentTimeMillis()
        )
    }
}

