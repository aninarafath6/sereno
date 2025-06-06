package com.example.sereno.features.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.common.utils.DateUtils
import com.example.sereno.databinding.BotChatLoadingBinding
import com.example.sereno.databinding.ChatItemBinding
import com.example.sereno.databinding.DateItemBinding
import com.example.sereno.features.chat.data.model.Chat
import com.example.sereno.features.chat.domain.model.ChatItemContent
import com.example.sereno.features.chat.domain.model.ChatItemContent.Companion.CHAT_ITEM_TYPE
import com.example.sereno.features.chat.domain.model.ChatItemContent.Companion.LOADING_ITEM_TYPE
import com.example.sereno.features.chat.ui.view_holder.ChatViewHolder
import com.example.sereno.features.chat.ui.view_holder.DateViewHolder
import com.example.sereno.features.chat.ui.view_holder.LoadingViewHolder
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val chats = mutableListOf<ChatItemContent>()
    private var scrollToPosition: ((position: Int) -> Unit)? = null
    private var onDoubleTapChat: ((chat:Chat) -> Unit)? = null
    private var blinkAtPosition: Int? = null

    override fun getItemViewType(position: Int): Int {
        val chat = chats[position]
        return chat.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = when (viewType) {
            CHAT_ITEM_TYPE -> ChatViewHolder(
                ChatItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            LOADING_ITEM_TYPE -> LoadingViewHolder(
                BotChatLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> DateViewHolder(
                DateItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LoadingViewHolder -> {}
            is ChatViewHolder -> {
                holder.bind(
                    position,
                    chats,
                    position == blinkAtPosition,
                    onClickChat = { id ->
                        if (id == null) return@bind
                        val posOfReplayChat =
                            chats.indexOfLast { it is ChatItemContent.ChatItem && it.chat.id == id }
                        blinkAtPosition = posOfReplayChat
                        scrollToPosition?.invoke(posOfReplayChat)
                        notifyItemChanged(posOfReplayChat)
                    },
                    onDoubleTap = onDoubleTapChat,
                    clearBlinkAnimation = {
                        blinkAtPosition = null
                    })
            }

            is DateViewHolder -> holder.bind((chats[position] as ChatItemContent.DateItem).formattedDate)
        }
    }

    override fun getItemCount(): Int = chats.size

    private fun shouldShowDateSeparator(current: Chat, previous: Chat?): Boolean {
        if (previous == null) return true


        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = current.createdAt
        }
        val previousCalendar = Calendar.getInstance().apply {
            timeInMillis = previous.createdAt
        }

        return !DateUtils.isSameDay(currentCalendar, previousCalendar)
    }

    fun setChats(chats: List<Chat>) {
        this.chats.clear()
        val chatContent: MutableList<ChatItemContent> = mutableListOf()
        chats.forEachIndexed { i, chat ->
            val prevChat = chats.getOrNull(i - 1)
            if (shouldShowDateSeparator(chat, prevChat)) {
                chatContent.add(ChatItemContent.DateItem(DateUtils.formatDate(chat.createdAt)))
            }
            chatContent.add(ChatItemContent.ChatItem(chat))
        }
        this.chats.addAll(chatContent)
        notifyDataSetChanged()
    }

    fun addChat(chat: Chat) {
        removeLoading()
        val lastChat = chats.lastOrNull()?.let {
            if (it is ChatItemContent.ChatItem) it.chat else null
        }

        if (shouldShowDateSeparator(chat, lastChat)) {
            chats.add(ChatItemContent.DateItem(DateUtils.formatDate(chat.createdAt)))
            chats.add(ChatItemContent.ChatItem(chat))
            notifyItemRangeInserted(chats.size - 2, 2)
        } else {
            chats.add(ChatItemContent.ChatItem(chat))
            notifyItemInserted(chats.size - 1)
        }
    }

    fun removeLoading() {
        val lastChat = chats.indexOfLast { it is ChatItemContent.Loading }
        if (lastChat == -1) return
        chats.removeAt(lastChat)
        notifyItemRemoved(chats.size)
    }

    fun addLoading() {
        chats.add(ChatItemContent.Loading)
        notifyItemInserted(chats.lastIndex)
    }

    fun isBot(position: Int): Boolean {
        val items = chats.getOrNull(position)
        return items is ChatItemContent.ChatItem && items.chat.isBot
    }

    fun isSwipeable(position: Int): Boolean {
        val items = chats.getOrNull(position)
        return items is ChatItemContent.ChatItem
    }

    fun setOnDoubleTapChat(onDoubleTap: (chat: Chat) -> Unit) {
        this.onDoubleTapChat = onDoubleTap
    }

    fun getChat(pos: Int): Chat? {
        val chat = chats.getOrNull(pos)
        return if (chat is ChatItemContent.ChatItem) chat.chat else null
    }

    fun setScrollListener(listener: ((position: Int) -> Unit)?) {
        this.scrollToPosition = listener
    }
}