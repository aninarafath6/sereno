package com.example.sereno.chat.views

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.R
import com.example.sereno.chat.adapter.ChatAdapter
import com.example.sereno.chat.events.ChatEvent
import com.example.sereno.chat.utils.ReplaySwiperHelper
import com.example.sereno.chat.view_model.ChatViewModel
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
        binding.field.sendButton.setOnClickListener(::sendMessage)

        binding.field.et.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                showSendButton()
            } else {
                hideSendButton()
            }
        }

        binding.field.releaseReplay.onClickWithHaptics {
            vm.setSwipedChat(null)
        }
    }

    private fun initChats() {
        binding.chats.adapter = chatAdapter
        binding.chats.layoutManager = LinearLayoutManager(this)
        binding.chats.setHasFixedSize(true)
        vm.onEvent(ChatEvent.LoadChats)
//        chatAdapter.setScrollToPositionListener { position, smoothScroll ->
//            if (smoothScroll) {
//                binding.chats.smoothScrollToPosition(position)
//            } else {
//                binding.chats.scrollToPosition(position)
//            }
//            chatAdapter.blinkItemAtPos(position)
//        }

        val itemTouchHelper = ItemTouchHelper(ReplaySwiperHelper(chatAdapter, ::onSwipeChat))
        itemTouchHelper.attachToRecyclerView(binding.chats)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            vm.chats.collectLatest {
                if (it.consumeWhole) {
                    chatAdapter.setChats(it.chats)
                } else {
                    chatAdapter.addChat(it.chats.lastOrNull() ?: return@collectLatest)
                    binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                scrollToBottom()
            }
        }

        vm.isLoading.observe(this) {
            binding.heading.online.text = if (it) "Typing..." else "Online"
            if (it) {
                chatAdapter.addLoading()
                scrollToBottom()
            } else {
                chatAdapter.removeLoading()
            }
        }
        vm.selectedChat.observe(this) {
            binding.field.replayPreviewContainer.isVisible = it != null
            binding.field.replayChatReference.text = it?.message
            binding.field.replayTo.text = ChatItem.getReplayToString(it?.isBot == true)
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

    private fun sendMessage(view: View) {
        binding.chats.scrollToPosition(chatAdapter.itemCount - 1)
        val composedMessage = binding.field.et.text.toString()
        vm.onEvent(ChatEvent.SendMessage(composedMessage))
        binding.field.et.text.clear()
        vm.setSwipedChat(null)
    }

    private fun scrollToBottom() {
        binding.chats.scrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener { }
    }

    fun onSwipeChat(pos: Int) {
        binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        val chat = chatAdapter.getChat(pos)
        vm.setSwipedChat(chat)
        binding.chats.adapter?.notifyItemChanged(pos)
    }
}