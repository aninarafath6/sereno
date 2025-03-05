package com.example.sereno.features.chat.domain

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.features.chat.domain.model.ChatState
import com.example.sereno.features.chat.data.model.Chat
import com.example.sereno.features.chat.data.repo.ChatRepo
import com.example.sereno.features.chat.data.repo.ChatResponse
import com.example.sereno.features.chat.data.room.ChatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dao: ChatsDao
) : ViewModel() {
    private val chatRepo: ChatRepo = ChatRepo()

    private val _isLoading = MutableLiveData(false)
    private val _selectedChat = MutableLiveData<Chat?>()
    private val _chats = MutableStateFlow(ChatState())
    private val botResponseQueue = ArrayDeque<Chat>()
    private var isProcessingChats = false

    val isLoading: LiveData<Boolean> = _isLoading
    val selectedChat: LiveData<Chat?> = _selectedChat
    val chats: StateFlow<ChatState> = _chats

    fun sendMessage(context: Context, composedMessage: String) {
        if (composedMessage.trim().isBlank()) return
        viewModelScope.launch {
            val userChat = Chat(
                message = composedMessage,
                replayChatId = _selectedChat.value?.id,
                isBot = false,
                createdAt = System.currentTimeMillis()
            )

            saveAndUpdateChat(userChat)

            val replayTo = if (userChat.replayChatId != null) {
                chats.value.chats.lastOrNull { it.id == userChat.replayChatId }
            } else null

            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val contextChats =
                    chats.value.chats.takeLast(CONTEXT_CHAT_WINDOW_SIZE) ?: emptyList()
                val chatResponse = chatRepo.chat(
                    context,
                    userChat,
                    contextChat = contextChats,
                    replayTo
                )
                val response = when (chatResponse) {
                    is ChatResponse.Failed -> listOf(
                        Chat.generateErrorChat(
                            chatResponse.message,
                            userChat.id
                        )
                    )

                    is ChatResponse.Success -> chatResponse.response
                }
                enQueueChat(response)
            }
            _isLoading.value = false
            _selectedChat.value = null
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            val chats = withContext(Dispatchers.IO) { dao.getChats() }
            _chats.value =
                ChatState(chats, consumeWhole = true)
            if (chats.isEmpty()) {
                saveAndUpdateChat(NEW_BOT_CHAT, consumeWhole = true)
            }
        }
    }

    private fun enQueueChat(chat: List<Chat>) {
        botResponseQueue.addAll(chat)
        if (isProcessingChats) return
        processChats()
    }

    private fun processChats() {
        isProcessingChats = true
        viewModelScope.launch {
            while (botResponseQueue.isNotEmpty()) {
                _isLoading.value = true
                delay(1000)
                _isLoading.value = false
                val chat = botResponseQueue.removeFirst()
                saveAndUpdateChat(chat)
            }
            isProcessingChats = false
        }
    }

    private suspend fun saveAndUpdateChat(chat: Chat, consumeWhole: Boolean = false) {
        // Todo: Handle exceptions, like what if user has no enough space to store the chat, etc..
        withContext(Dispatchers.IO) {
            dao.saveChat(chat)
        }
        withContext(Dispatchers.Main) {
            _chats.value = _chats.value.copy(
                chats = _chats.value.chats.plus(chat),
                consumeWhole = consumeWhole
            )
        }
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
        private const val CONTEXT_CHAT_WINDOW_SIZE = 50
    }
}
