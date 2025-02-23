package com.example.sereno.chat.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sereno.chat.model.ChatModel
import com.example.sereno.chat.model.ChatOwner
import java.util.Calendar

class ChatViewModel : ViewModel() {
    private val _chats = MutableLiveData<List<ChatModel>>()

    fun chats(): LiveData<List<ChatModel>> = _chats

    fun setInitialChats() {
        _chats.value = listOf(
            ChatModel(
                "Hey, I'm your personalized AI therapist. Feel free to share whatever's on your mind—I'm here to listen and support you",
                ChatOwner.BOT,
                Calendar.getInstance().timeInMillis
            ),
        )
    }

    fun sendMessage(value: String) {
        if (value.isEmpty()) return

        val newUserChat =
            ChatModel(value.trim(), ChatOwner.USER, Calendar.getInstance().timeInMillis)
        _chats.value = _chats.value?.plus(newUserChat)
    }
}