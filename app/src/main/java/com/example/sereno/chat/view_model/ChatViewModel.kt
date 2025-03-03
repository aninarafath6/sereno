package com.example.sereno.chat.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.ChatState
import com.example.sereno.chat.repo.ChatResponse
import com.example.sereno.chat.repo.GroqRepo
import com.example.sereno.chat.repo.room.ChatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val _oldChats = MutableLiveData<ChatState>()
    private val _newChats = MutableLiveData<ChatState>()
    private val botResponseQueue = ArrayDeque<Chat>()
    private var isProcessingChats = false

    val isLoading: LiveData<Boolean> = _isLoading
    val selectedChat: LiveData<Chat?> = _selectedChat
    val chats = MediatorLiveData<ChatState>().apply {
        addSource(_oldChats) {}
        addSource(_newChats) {}
    }

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
                chats.value?.chats?.lastOrNull { it.id == userChat.replayChatId }
            } else null

            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val contextChats = if ((chats.value?.chats?.size
                        ?: 0) < contextChatWindowSize
                ) chats.value?.chats else chats.value?.chats?.
                val chatResponse =
                    groqRepo.chat(
                        context,
                        userChat,
                        contextChat = _chats.value.chats.subList(0, _chats.value.chats.size - 1),
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
            _oldChats.value = ChatState(chats, consumeWhole = true)
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
            _newChats.value = _newChats.value?.copy(
                chats = _newChats.value?.chats?.plus(chat) ?: listOf(chat),
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
        private const val contextChatWindowSize = 50
    }
}
