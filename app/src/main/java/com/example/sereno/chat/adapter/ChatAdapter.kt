package com.example.sereno.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.isUser
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ChatItemBinding
import com.example.sereno.databinding.DateItemBinding
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val chats = mutableListOf<Chat>()
    private var scrollToPosition: ((position: Int, smoothScroll: Boolean) -> Unit)? = null
    private var blinkAtPosition: Int? = null

    private val displayItems = mutableListOf<DisplayItem>()

    companion object {
        private const val CHAT = 0
        private const val DATE = 1
    }

    sealed class DisplayItem {
        data class ChatItem(val chat: Chat) : DisplayItem()
        data class DateItem(val chat: Chat) : DisplayItem()
    }

    inner class ChatVH(private val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val displayItem = displayItems[adapterPosition] as DisplayItem.ChatItem
            val chat = displayItem.chat
            binding.user.isVisible = !chat.isBot
            binding.bot.isVisible = chat.isBot

            var replayChat: Chat? = null

            val prevChatPos = findPreviousChatPosition(adapterPosition)
            val prevChat = if (prevChatPos != -1) {
                (displayItems[prevChatPos] as DisplayItem.ChatItem).chat
            } else null

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

    inner class DateVH(private val binding: DateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val displayItem = displayItems[adapterPosition] as DisplayItem.DateItem
            val chat = displayItem.chat

            val chatCalendar = Calendar.getInstance().apply {
                timeInMillis = chat.createdAt
            }

            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            val dateText = when {
                isSameDay(chatCalendar, today) -> {
                    "Today"
                }
                isSameDay(chatCalendar, yesterday) -> {
                    "Yesterday"
                }
                isWithinLastWeek(chatCalendar, today) -> {
                    val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
                    dayFormat.format(chatCalendar.time)
                }
                chatCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
                    val dateFormat = java.text.SimpleDateFormat("EEE, d MMM", java.util.Locale.getDefault())
                    dateFormat.format(chatCalendar.time)
                }
                else -> {
                    val dateFormat = java.text.SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale.getDefault())
                    dateFormat.format(chatCalendar.time)
                }
            }

            binding.date.text = dateText
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isWithinLastWeek(chatDate: Calendar, today: Calendar): Boolean {
            val sixDaysAgo = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, -6) // 6 days ago (today + yesterday + 5 more days = 7 days total)
            }

            return chatDate.after(sixDaysAgo) && chatDate.before(today)
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
        val vh = when (viewType) {
            CHAT -> ChatVH(
                ChatItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            DATE -> DateVH(
                DateItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatVH -> {
                holder.bind()
            }

            is DateVH -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    private fun shouldShowDate(index: Int): Boolean {
        if (index == 0) return true

        val currentChat = chats[index]
        val previousChat = chats[index - 1]

        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = currentChat.createdAt
        }
        val previousCalendar = Calendar.getInstance().apply {
            timeInMillis = previousChat.createdAt
        }

        return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun rebuildDisplayItems() {
        displayItems.clear()

        chats.forEachIndexed { index, chat ->
            if (shouldShowDate(index) || index == 0) {
                displayItems.add(DisplayItem.DateItem(chat))
            }
            displayItems.add(DisplayItem.ChatItem(chat))
        }
    }

    fun setChats(chats: List<Chat>) {
        this.chats.clear()
        this.chats.addAll(chats)

        rebuildDisplayItems()
        notifyDataSetChanged()
    }

    fun addChat(chat: Chat) {
        val oldSize = displayItems.size
        chats.add(chat)

        val needsDateHeader = chats.size > 1 && shouldShowDate(chats.size - 1)

        if (needsDateHeader) {
            displayItems.add(DisplayItem.DateItem(chat))
        }
        displayItems.add(DisplayItem.ChatItem(chat))

        // If we added both a date and a chat
        if (needsDateHeader) {
            notifyItemRangeInserted(oldSize, 2)
        } else {
            notifyItemInserted(oldSize)
        }
    }

    fun isBot(position: Int): Boolean {
        val item = displayItems.getOrNull(position)
        return if (item is DisplayItem.ChatItem) {
            item.chat.isBot
        } else false
    }

    fun getChat(position: Int): Chat? {
        return when (val item = displayItems.getOrNull(position)) {
            is DisplayItem.ChatItem -> item.chat
            is DisplayItem.DateItem -> null
            null -> null
        }
    }

    fun isChat(position: Int):Boolean{
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