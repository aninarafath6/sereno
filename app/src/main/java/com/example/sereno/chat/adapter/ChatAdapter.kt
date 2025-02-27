package com.example.sereno.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.Chat
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ChatItemBinding
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.VH>() {
    private val chats = mutableListOf<Chat>()
    private var scrollToPosition: ((position: Int, smoothScroll: Boolean) -> Unit)? = null
    private var blinkAtPosition: Int? = null

    inner class VH(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val chat = chats[adapterPosition]
            binding.user.isVisible = !chat.isBot
            binding.bot.isVisible = chat.isBot

            var replayChat: Chat? = null
            val prevChat = chats.getOrNull(adapterPosition - 1)

            if (shouldShowReplaySection(chat, prevChat)) {
                replayChat = chats.find { it.id == chat.replayChatId }
            }

            if (adapterPosition == blinkAtPosition) {
                binding.user.startBlinkAnimation()
                binding.bot.startBlinkAnimation()
                blinkAtPosition = null
            }

            binding.user.setChatText(chat, replayChat)
            binding.bot.setChatText(chat, replayChat)
            if (replayChat != null) {
                binding.user.onClickWithHaptics {
                    scrollToMentionedChat(chat)
                }
                binding.bot.onClickWithHaptics {
                    scrollToMentionedChat(chat)
                }
            }
        }
    }

    private fun scrollToMentionedChat(chat: Chat) {
        if (chat.replayChatId == null) return
        val index = chats.indexOfFirst { it.id == chat.replayChatId }
        scrollToPosition?.invoke(index, true)
    }

    private fun shouldShowReplaySection(chat: Chat, prevChat: Chat?): Boolean {
        if (chat.replayChatId == null) return false
        if (chat.replayChatId == prevChat?.id && !prevChat.isBot) return false

        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = chats.count()

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind()
    }

    private fun shouldShowDate(position: Int): Boolean {
        if (position == 0) return true

        val currentChat = chats[position]
        val previousChat = chats[position - 1]

        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = currentChat.createdAt
        }
        val previousCalendar = Calendar.getInstance().apply {
            timeInMillis = previousChat.createdAt
        }

        return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)
    }


    fun setChats(chats: List<Chat>) {
        this.chats.clear()
        this.chats.addAll(chats)
        notifyDataSetChanged()
    }

    fun addChat(chat: Chat) {
        chats.add(chat)
        notifyItemInserted(chats.size - 1)
    }

    fun isBot(position: Int): Boolean {
        return chats.getOrNull(position)?.isBot ?: false
    }

    fun getChat(position: Int): Chat? {
        return chats.getOrNull(position)
    }

    fun setScrollToPositionListener(listener: (position: Int, smoothScroll: Boolean) -> Unit) {
        scrollToPosition = listener
    }

    fun blinkItemAtPos(pos: Int) {
        blinkAtPosition = pos
        notifyItemChanged(pos)
    }
}