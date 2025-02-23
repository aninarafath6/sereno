package com.example.sereno.chat.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.R
import com.example.sereno.chat.adapter.ChatAdapter
import com.example.sereno.chat.view_model.ChatViewModel
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private val chatAdapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = getColor(R.color.primary)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        initChats()
        initObservers()
    }

    private fun initListeners() {
        binding.heading.back.onClickWithHaptics(::finish)
        binding.field.sendButton.onClickWithHaptics(::sendMessage)

        binding.field.et.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                showSendButton()
            } else {
                hideSendButton()
            }
        }
    }

    private fun initChats() {
        binding.chats.adapter = chatAdapter
        binding.chats.layoutManager = LinearLayoutManager(this)
        vm.setInitialChats()
    }

    private fun initObservers() {
        vm.chats().observe(this) {
            chatAdapter.setChats(it)
            binding.chats.scrollToPosition(it.size - 1)
        }
    }

    private fun showSendButton() {
        if (binding.field.sendButton.isVisible) return
        binding.field.sendButton.scaleX = .5f
        binding.field.sendButton.scaleY = .5f
        binding.field.sendButton.isVisible = true
        binding.field.sendButton.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
    }

    private fun hideSendButton() {
        if (!binding.field.sendButton.isVisible) return
        binding.field.sendButton.animate().scaleX(0f).scaleY(.0f).setDuration(200).withEndAction {
            binding.field.sendButton.isVisible = false
        }
    }

    private fun sendMessage() {
        vm.sendMessage(binding.field.et.text.toString())
        binding.field.et.text.clear()
    }
}