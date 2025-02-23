package com.example.sereno.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.ChatModel
import com.example.sereno.chat.model.ChatOwner
import com.example.sereno.databinding.ChatItemBinding
import java.util.Calendar

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.VH>() {
    private val chats = mutableListOf<ChatModel>()

    inner class VH(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val chat = chats[adapterPosition]
            binding.bot.isVisible = chat.owner == ChatOwner.BOT
            binding.user.isVisible = chat.owner == ChatOwner.USER
            if (chat.owner == ChatOwner.BOT) {
                binding.botResponse.text = chat.chat
            } else {
                binding.userMessage.text = chat.chat
            }
            binding.date.isVisible = shouldShowDate(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            timeInMillis = currentChat.dateEpoch
        }
        val previousCalendar = Calendar.getInstance().apply {
            timeInMillis = previousChat.dateEpoch
        }

        return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)
    }

    fun setChats(chats: List<ChatModel>) {
        this.chats.clear()
        this.chats.addAll(chats)
        notifyDataSetChanged()
    }
}