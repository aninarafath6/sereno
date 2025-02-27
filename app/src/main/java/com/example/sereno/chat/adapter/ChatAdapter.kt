package com.example.sereno.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.isUser
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.common.utils.DateUtils
import com.example.sereno.databinding.ChatItemBinding
import com.example.sereno.databinding.DateItemBinding
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val chats = mutableListOf<Chat>()
    private val displayItems = mutableListOf<DisplayItem>()
    private var scrollToPosition: ((position: Int, smoothScroll: Boolean) -> Unit)? = null
    private var blinkAtPosition: Int? = null

    companion object {
        private const val CHAT = 0
        private const val DATE = 1
    }

    sealed class DisplayItem {
        data class ChatItem(val chat: Chat) : DisplayItem()
        data class DateItem(val chat: Chat, val formattedDate: String) : DisplayItem()
    }

    inner class ChatVH(private val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val displayItem = displayItems[position] as DisplayItem.ChatItem
            val chat = displayItem.chat

            binding.user.isVisible = !chat.isBot
            binding.bot.isVisible = chat.isBot

            val replayChat = findReplayChat(chat, position)

            if (position == blinkAtPosition) {
                binding.user.startBlinkAnimation()
                binding.bot.startBlinkAnimation()
                blinkAtPosition = null
            }

            binding.user.setChatText(chat, replayChat)
            binding.bot.setChatText(chat, replayChat)

            binding.user.apply {
                if (replayChat != null) {
                    onClickWithHaptics { scrollToMentionedChat(chat) }
                } else {
                    setOnClickListener(null)
                }
            }

            binding.bot.apply {
                if (replayChat != null) {
                    onClickWithHaptics { scrollToMentionedChat(chat) }
                } else {
                    setOnClickListener(null)
                }
            }

        }

        private fun findReplayChat(chat: Chat, position: Int): Chat? {
            if (chat.replayChatId == null) return null

            val prevChatPos = findPreviousChatPosition(position)
            val prevChat = if (prevChatPos != -1) {
                (displayItems[prevChatPos] as DisplayItem.ChatItem).chat
            } else null

            if (shouldShowReplaySection(chat, prevChat)) {
                return chats.find { it.id == chat.replayChatId }
            }

            return null
        }
    }

    inner class DateVH(private val binding: DateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val displayItem = displayItems[position] as DisplayItem.DateItem
            binding.date.text = displayItem.formattedDate
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.ChatItem -> CHAT
            is DisplayItem.DateItem -> DATE
        }
    }

    private fun scrollToMentionedChat(chat: Chat) {
        if (chat.replayChatId == null) return

        val displayPosition = displayItems.indexOfFirst {
            it is DisplayItem.ChatItem && it.chat.id == chat.replayChatId
        }

        if (displayPosition != -1) {
            scrollToPosition?.invoke(displayPosition, true)
        }
    }

    private fun shouldShowReplaySection(chat: Chat, prevChat: Chat?): Boolean {
        if (chat.replayChatId == null) return false
        if (chat.replayChatId == prevChat?.id && prevChat.isUser()) return false
        return true
    }

    private fun findPreviousChatPosition(currentPosition: Int): Int {
        for (i in (currentPosition - 1) downTo 0) {
            if (displayItems[i] is DisplayItem.ChatItem) {
                return i
            }
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CHAT -> ChatVH(ChatItemBinding.inflate(inflater, parent, false))
            DATE -> DateVH(DateItemBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatVH -> holder.bind(position)
            is DateVH -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = displayItems.size

    private fun rebuildDisplayItems() {
        displayItems.clear()

        chats.forEachIndexed { index, chat ->
            val previousChat = chats.getOrNull(index - 1)

            if (shouldShowDateSeparator(chat, previousChat)) {
                val formattedDate = DateUtils.formatDate(chat.createdAt)
                displayItems.add(DisplayItem.DateItem(chat, formattedDate))
            }

            displayItems.add(DisplayItem.ChatItem(chat))
        }
    }

    private fun shouldShowDateSeparator(current: Chat, previous: Chat?): Boolean {
        if (previous == null) return true

        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = current.createdAt
        }
        val previousCalendar = Calendar.getInstance().apply {
            timeInMillis = previous.createdAt
        }

        return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)
    }

    fun setChats(chatList: List<Chat>) {
        chats.clear()
        chats.addAll(chatList)
        rebuildDisplayItems()
        notifyDataSetChanged()
    }

    fun addChat(chat: Chat) {
        val oldSize = displayItems.size
        chats.add(chat)

        val previousChat = chats.getOrNull(chats.size - 2)
        val needsDateHeader = shouldShowDateSeparator(chat, previousChat)

        if (needsDateHeader) {
            val formattedDate = DateUtils.formatDate(chat.createdAt)
            displayItems.add(DisplayItem.DateItem(chat, formattedDate))
        }

        displayItems.add(DisplayItem.ChatItem(chat))

        if (needsDateHeader) {
            notifyItemRangeInserted(oldSize, 2)
        } else {
            notifyItemInserted(oldSize)
        }
    }

    fun isBot(position: Int): Boolean {
        return when (val item = displayItems.getOrNull(position)) {
            is DisplayItem.ChatItem -> item.chat.isBot
            else -> false
        }
    }

    fun getChat(position: Int): Chat? {
        return when (val item = displayItems.getOrNull(position)) {
            is DisplayItem.ChatItem -> item.chat
            is DisplayItem.DateItem -> null
            null -> null
        }
    }

    fun isChat(position: Int): Boolean {
        return displayItems.getOrNull(position) is DisplayItem.ChatItem
    }

    fun setScrollToPositionListener(listener: (position: Int, smoothScroll: Boolean) -> Unit) {
        scrollToPosition = listener
    }

    fun blinkItemAtPos(pos: Int) {
        blinkAtPosition = pos
        notifyItemChanged(pos)
    }
}