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
        viewModelScope.launch {
            when (event) {
                is ChatEvent.BotResponded -> handleBotResponse(event)
                ChatEvent.LoadChats -> loadChats()
                is ChatEvent.SendMessage -> sendMessage(event)
            }
        }
    }

    private suspend fun sendMessage(event: ChatEvent.SendMessage) {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            val chatMessage = when (val chatResponse = groqRepo.chat(event.message)) {
                is ChatResponse.Failed -> "Sorry, I'm not able to respond to that."
                is ChatResponse.Success -> chatResponse.response
            }

            val chat = Chat(
                message = chatMessage,
                isBot = false,
                createdAt = System.currentTimeMillis()
            )
            // Todo: Handle exceptions, like what if user has no enough space to store the chat, etc..
            saveAndUpdateChat(chat)

        }
        _isLoading.value = false
    }

    private suspend fun loadChats() {
        val chats = withContext(Dispatchers.IO) {
            dao.getChats()
        }.value ?: emptyList()
        _chats.value = ChatState(chats, consumeWhole = true)
    }

    private suspend fun handleBotResponse(botResponse: ChatEvent.BotResponded) {
        val botChat = Chat(
            message = botResponse.message,
            isBot = true,
            createdAt = System.currentTimeMillis()
        )
        saveAndUpdateChat(botChat)
    }

    private suspend fun saveAndUpdateChat(chat: Chat) {
        withContext(Dispatchers.IO) {
            dao.saveChat(chat)
        }
        _chats.value = _chats.value.copy(chats = _chats.value.chats + chat, consumeWhole = false)
    }
}

