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

    private val _isLoading = MutableLiveData(false)
    private val _selectedChat = MutableLiveData<Chat?>()
    private val _chats = MutableStateFlow(ChatState())

    val isLoading: LiveData<Boolean> = _isLoading
    val selectedChat: LiveData<Chat?> = _selectedChat
    val chats: StateFlow<ChatState> = _chats

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.BotResponded -> handleBotResponse(event)
            ChatEvent.LoadChats -> loadChats()
            is ChatEvent.SendMessage -> sendMessage(event)
            is ChatEvent.SwipeToReplay -> {}
        }
    }

    private fun sendMessage(event: ChatEvent.SendMessage) {
        if (event.message.trim().isBlank()) return

        viewModelScope.launch {
            val userChat = Chat(
                message = event.message.trim(),
                replayChatId = _selectedChat.value?.id,
                isBot = false,
                createdAt = System.currentTimeMillis()
            )
            saveAndUpdateChat(userChat)
            _isLoading.value = true

            val replayTo = if (userChat.replayChatId != null) {
                chats.value.chats.lastOrNull { it.id == userChat.replayChatId }
            } else null

            withContext(Dispatchers.IO) {
                val response = when (val chatResponse =
                    groqRepo.chat(
                        userChat.message,
                        contextChat = _chats.value.chats.subList(0, _chats.value.chats.size - 2),
                        replayTo
                    )) {
                    is ChatResponse.Failed -> listOf("Sorry, I'm not able to respond to that.")
                    is ChatResponse.Success -> chatResponse.response
                }
                onEvent(ChatEvent.BotResponded(response, replayChat = userChat.id))
            }
            _selectedChat.value = null
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            val chats = withContext(Dispatchers.IO) { dao.getChats() }
            _chats.value = ChatState(chats, consumeWhole = true)
            if (chats.isEmpty()) {
                saveAndUpdateChat(NEW_BOT_CHAT, consumeWhole = true)
            }
        }
    }

    private fun handleBotResponse(botResponse: ChatEvent.BotResponded) {
        var chatId = botResponse.replayChat
        viewModelScope.launch {
            botResponse.messages.forEach {
                val botChat = Chat(
                    message = it,
                    replayChatId = chatId,
                    isBot = true,
                    createdAt = System.currentTimeMillis()
                )
                chatId = botChat.id
                saveAndUpdateChat(botChat)
            }
            _isLoading.value = false
        }
    }

    private suspend fun saveAndUpdateChat(chat: Chat, consumeWhole: Boolean = false) {
        // Todo: Handle exceptions, like what if user has no enough space to store the chat, etc..
        withContext(Dispatchers.IO) {
            dao.saveChat(chat)
        }
        _chats.value = _chats.value.copy(
            chats = _chats.value.chats + chat,
            consumeWhole = consumeWhole
        )
    }

    fun setSwipedChat(chat: Chat?) {
        _selectedChat.value = chat
    }


    companion object {
        private val NEW_BOT_CHAT = Chat(
            message = "Hello, I'm Jhon. How can I help you?",
            isBot = true,
            createdAt = System.currentTimeMillis()
        )
    }
}

