package com.example.sereno.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.Chat
import com.example.sereno.databinding.ChatItemBinding
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.VH>() {
    private val chats = mutableListOf<Chat>()


    inner class VH(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val chat = chats[adapterPosition]
            binding.user.isVisible = !chat.isBot
            binding.bot.isVisible = chat.isBot
            binding.date.isVisible = shouldShowDate(adapterPosition)
            binding.extraMargin.isVisible = false

            val prevChat = chats.getOrNull(adapterPosition - 1)
            var replayChat: Chat? = null
            Log.d("ChatAdapter", "id c: ${prevChat?.id} id prev: ${chat.id}")
            if (chat.replayChatId != prevChat?.id) {
                replayChat = chats.find { it.id == chat.replayChatId }
            }

            binding.user.setChatText(chat, replayChat)
            binding.bot.setChatText(chat, replayChat)
        }
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
}