package com.example.sereno.chat.view_holder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.ChatItemContent
import com.example.sereno.chat.model.isUser
import com.example.sereno.databinding.ChatItemBinding

class ChatViewHolder(private val binding: ChatItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(pos: Int, chats: List<ChatItemContent>) {
        resetViews()
        val chat = chats[pos] as? ChatItemContent.ChatItem ?: return
        val shouldShowReplayUi = shouldShowReplaySection(chat.chat, getLastChat(chat.chat, chats))
        val chatView = if (chat.chat.isUser()) binding.user else binding.bot
        val replayChat = if (shouldShowReplayUi) findReplayChat(chat.chat, chats) else null

        chatView.isVisible = true
        chatView.setChatText(chat.chat, replayChat)
    }

    private fun resetViews() {
        binding.bot.isVisible = false
        binding.user.isVisible = false
    }

    private fun getLastChat(currentChat: Chat?, chats: List<ChatItemContent>): Chat? {
        return chats.asReversed()
            .find { (it is ChatItemContent.ChatItem && it.chat.id != currentChat?.id) }
            ?.let { (it as ChatItemContent.ChatItem).chat }
    }

    private fun shouldShowReplaySection(chat: Chat, prevChat: Chat?): Boolean {
        if (chat.replayChatId == null) return false
        if (chat.replayChatId == prevChat?.id && prevChat.isUser()) return false
        return true
    }

    private fun findReplayChat(chat: Chat, chats: List<ChatItemContent>): Chat? {
        val replayChatItem =
            chats.lastOrNull { it is ChatItemContent.ChatItem && it.chat.id == chat.replayChatId } as? ChatItemContent.ChatItem
        return replayChatItem?.chat
    }
}