package com.example.sereno.features.chat.ui

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityChatBinding
import com.example.sereno.features.chat.ui.adapter.ChatAdapter
import com.example.sereno.features.chat.utils.ReplaySwiperHelper
import com.example.sereno.features.chat.domain.ChatViewModel
import com.example.sereno.features.home.ui.BuyPremiumBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private val chatAdapter = ChatAdapter()

    companion object {
        private const val ANIMATION_DURATION = 400L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = getColor(R.color.primary)
        binding = ActivityChatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setupWindowInsets()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        initListeners()
        initChatsRecyclerView()
        initObservers()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.heading.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                0,
                systemBars.top,
                0,
                resources.getDimensionPixelSize(R.dimen._12dp)
            )
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            view.setPadding(
                0,
                0,
                0,
                bottomPadding
            )
            insets
        }
    }

    private fun initChatsRecyclerView() {
        binding.chats.adapter = chatAdapter
        binding.chats.layoutManager = LinearLayoutManager(this)
        binding.chats.setHasFixedSize(true)
        vm.loadChats()

        chatAdapter.setScrollListener { position ->
            binding.chats.smoothScrollToPosition(position)
        }
        chatAdapter.setOnDoubleTapChat {
            vm.setSwipedChat(it)
        }
        binding.chats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val itemCount = recyclerView.adapter?.itemCount ?: 0

                // Show button when not at bottom (with threshold of 2 items)
                val isNearBottom = lastVisibleItemPosition >= itemCount - 3

                if (isNearBottom) hideFloatingScrollButton() else showFloatingScrollButton()
            }
        })

        val itemTouchHelper = ItemTouchHelper(ReplaySwiperHelper(chatAdapter, ::onSwipeChat))
        itemTouchHelper.attachToRecyclerView(binding.chats)
    }

    private fun initListeners() {
        binding.heading.back.onClickWithHaptics(::finish)
        binding.field.sendButton.setOnClickListener(::sendMessage)
        binding.heading.call.onClickWithHaptics {
            showPremiumBottomSheet()
        }
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
        binding.goToBottom.root.onClickWithHaptics {
            scrollToBottom(true)
        }
    }

    private fun showPremiumBottomSheet() {
        val bottomSheet =
            BuyPremiumBottomSheet()
        bottomSheet.show(
            supportFragmentManager,
            "ModalBottomSheet"
        )
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
        vm.sendMessage(this, composedMessage)
        binding.field.et.text.clear()
        vm.setSwipedChat(null)
    }

    private fun scrollToBottom(smooth: Boolean = false) {
        val pos = chatAdapter.itemCount - 1
        if (smooth) {
            binding.chats.smoothScrollToPosition(pos)
        } else {
            binding.chats.scrollToPosition(pos)
        }
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

    private fun hideFloatingScrollButton() {
        if (!binding.goToBottom.root.isVisible) return
        binding.goToBottom.root.translationX = 0f
        binding.goToBottom.root.animate().translationX(200f).setDuration(ANIMATION_DURATION)
            .withEndAction {
                binding.goToBottom.root.isVisible = false
            }
    }

    private fun showFloatingScrollButton() {
        if (binding.goToBottom.root.isVisible) return
        binding.goToBottom.root.isVisible = true
        binding.goToBottom.root.translationX = 200f
        binding.goToBottom.root.animate().translationX(0f).setDuration(ANIMATION_DURATION).start()
    }
}