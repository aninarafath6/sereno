package com.example.sereno.features.chat.domain.model

import com.example.sereno.features.chat.data.model.Chat

sealed class ChatItemContent(val id: Int) {
    data class ChatItem(val chat: Chat) : ChatItemContent(CHAT_ITEM_TYPE)
    data object Loading : ChatItemContent(LOADING_ITEM_TYPE)
    data class DateItem(val formattedDate: String) : ChatItemContent(DATE_ITEM_TYPE)
    companion object {
        const val CHAT_ITEM_TYPE = 0
        const val DATE_ITEM_TYPE = 1
        const val LOADING_ITEM_TYPE = 2
    }
}
