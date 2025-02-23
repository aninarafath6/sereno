package com.example.sereno.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.model.ChatModel
import com.example.sereno.chat.model.ChatOwner
import com.example.sereno.databinding.ChatItemBinding

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
            binding.marginTop.isVisible = shouldShowTopMargin(adapterPosition)
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

    private fun shouldShowTopMargin(position: Int): Boolean {
        return position == 0 || chats[position].owner == chats[position - 1].owner
    }

    fun setChats(chats: List<ChatModel>) {
        this.chats.clear()
        this.chats.addAll(chats)
        notifyDataSetChanged()
    }
}