package com.example.sereno.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.Chat
import com.example.sereno.chat.model.isUser
import com.example.sereno.common.utils.DateUtils
import com.example.sereno.databinding.BotResponseLoadingBinding
import com.example.sereno.databinding.ChatItemBinding
import com.example.sereno.databinding.DateItemBinding
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val chats = mutableListOf<Chat>()
    private val displayItems = mutableListOf<DisplayItem>()
    private var scrollToPosition: ((position: Int, smoothScroll: Boolean) -> Unit)? = null
    private var blinkAtPosition: Int? = null
    private var shouldShowBotResponseLoading = false

    sealed class DisplayItem {
        data class ChatItem(val chat: Chat) : DisplayItem()
        data class DateItem(val chat: Chat, val formattedDate: String) : DisplayItem()
    }

    inner class ChatVH(private val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val displayItem = displayItems[position] as DisplayItem.ChatItem
            val chat = displayItem.chat
            val replayChat = findReplayChat(chat, position)
            val blink = position == blinkAtPosition

            ChatItemBinder(binding).bind(chat, replayChat, blink)

            binding.bot.setOnClickListener { scrollToMentionedChat(chat) }
            binding.user.setOnClickListener { scrollToMentionedChat(chat) }
        }
    }

    inner class DateVH(private val binding: DateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val displayItem = displayItems[position] as DisplayItem.DateItem
            binding.date.text = displayItem.formattedDate
        }
    }

    inner class BotResponseLoading(private val binding: BotResponseLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when {
            shouldShowBotResponseLoading && position == displayItems.size -> BOT_RESPONSE_LOADING
            position < displayItems.size && displayItems[position] is DisplayItem.ChatItem -> CHAT
            position < displayItems.size && displayItems[position] is DisplayItem.DateItem -> DATE
            else -> CHAT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CHAT -> ChatVH(ChatItemBinding.inflate(inflater, parent, false))
            DATE -> DateVH(DateItemBinding.inflate(inflater, parent, false))
            BOT_RESPONSE_LOADING -> BotResponseLoading(BotResponseLoadingBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "Binding view holder at position $position")
        when (holder) {
            is ChatVH -> {
                if (position < displayItems.size) {
                    holder.bind(position)
                } else {
                    Log.w(TAG, "Invalid position in onBindViewHolder: $position")
                }
            }
            is DateVH -> {
                if (position < displayItems.size) {
                    holder.bind(position)
                } else {
                    Log.w(TAG, "Invalid position in onBindViewHolder: $position")
                }
            }
            is BotResponseLoading -> {
                // No binding required for loading indicator
            }
        }
    }

    override fun getItemCount(): Int =
        displayItems.size + if (shouldShowBotResponseLoading) 1 else 0

    fun setChats(chatList: List<Chat>) {
        val oldDisplayItems = displayItems.toList()
        chats.clear()
        chats.addAll(chatList)
        rebuildDisplayItems()
        val diffResult = DiffUtil.calculateDiff(ChatDiffCallback(oldDisplayItems, displayItems))
        diffResult.dispatchUpdatesTo(this)
    }

    fun addChat(chat: Chat) {
        require(chat.id != null) { "Chat ID cannot be null" }

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

    fun setLoading(shouldShowLoading: Boolean) {
        this.shouldShowBotResponseLoading = shouldShowLoading
        notifyDataSetChanged()
    }

    fun getChat(position: Int): Chat? {
        return when (val item = displayItems.getOrNull(position)) {
            is DisplayItem.ChatItem -> item.chat
            else -> null
        }
    }

    fun blinkItemAtPos(pos: Int) {
        blinkAtPosition = pos
        notifyItemChanged(pos)
    }

    fun setScrollToPositionListener(listener: (position: Int, smoothScroll: Boolean) -> Unit) {
        scrollToPosition = listener
    }

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
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = current.createdAt }
        val previousCalendar = Calendar.getInstance().apply { timeInMillis = previous.createdAt }
        return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)
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

    private class ChatDiffCallback(
        private val oldList: List<DisplayItem>,
        private val newList: List<DisplayItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return when {
                oldItem is DisplayItem.ChatItem && newItem is DisplayItem.ChatItem -> oldItem.chat.id == newItem.chat.id
                oldItem is DisplayItem.DateItem && newItem is DisplayItem.DateItem -> oldItem.chat.id == newItem.chat.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private class ChatItemBinder(private val binding: ChatItemBinding) {
        fun bind(chat: Chat, replayChat: Chat?, blink: Boolean) {
            binding.user.isVisible = !chat.isBot
            binding.bot.isVisible = chat.isBot
            binding.user.setChatText(chat, replayChat)
            binding.bot.setChatText(chat, replayChat)
            if (blink) {
                binding.user.startBlinkAnimation()
                binding.bot.startBlinkAnimation()
            }
        }
    }

    fun isChat(pos:Int):Boolean{
        return displayItems[pos] is DisplayItem.ChatItem
    }

    fun isBot(pos:Int):Boolean{
        return (displayItems[pos] as? DisplayItem.ChatItem)?.chat?.isBot == true
    }
    companion object {
        private const val TAG = "ChatAdapter"
        private const val CHAT = 0
        private const val DATE = 1
        private const val BOT_RESPONSE_LOADING = 2
    }
}