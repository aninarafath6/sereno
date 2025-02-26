package com.example.sereno.chat.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.example.sereno.R
import com.example.sereno.chat.model.Chat
import com.example.sereno.databinding.ChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomViewStyleable")
class ChatItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defaultStyleAttr) {

    val binding: ChatBinding = ChatBinding.inflate(LayoutInflater.from(context), this, true)
    private var chatOwner = USER
    private var blinkJob: Job? = null

    companion object {
        private const val USER = 0
        private const val BOT = 1

        fun getReplayToString(isBot: Boolean): String {
            return if (isBot) "John- Ai Therapist" else "You"
        }
    }

    init {
        val styles =
            context.obtainStyledAttributes(attrs, R.styleable.ChatType, defaultStyleAttr, 0)
        chatOwner = styles.getInt(R.styleable.ChatType_owner, USER)
        Log.d("ChatItem", "Chat owner: $chatOwner")
        setBackground()
        styles.recycle()
    }

    fun setChatText(chat: Chat, replayChat: Chat? = null) {
        binding.response.text = chat.message
        binding.replayContainer.post {
            adjustReplayContainerConstraints()
        }
        binding.replayContainer.isVisible = replayChat != null
        binding.replay.text = replayChat?.message
        binding.replayTo.text = getReplayToString(replayChat?.isBot == true)

    }

    fun startBlinkAnimation() {
        blinkJob?.cancel()

        blinkJob = CoroutineScope(Dispatchers.Main).launch {
            val animator = ValueAnimator.ofFloat(1f, 0.3f, 1f).apply {
                duration = 500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    val alphaValue = it.animatedValue as Float
                    binding.chatContainer.alpha = alphaValue
                }
                start()
            }

            delay(1500)
            animator.cancel()
            binding.chatContainer.animate().alpha(1f).setDuration(200).start()
        }
    }


    private fun setBackground() {
        val background = if (chatOwner == BOT) {
            R.drawable.bot_chat_bg
        } else {
            R.drawable.user_chat_bg
        }

        val replayBackground = if (chatOwner == BOT) {
            R.drawable.bot_replay_bg
        } else {
            R.drawable.user_replay_bg
        }

        binding.root.background = null
        binding.root.setBackgroundResource(background)
        binding.replayBg.setBackgroundResource(replayBackground)
    }

    private fun adjustReplayContainerConstraints() {
        binding.root.doOnLayout {
            val widthOfReplay = binding.replayContainer.width
            val widthOfResponse = binding.chatContainer.width

            binding.replayContainer.updateLayoutParams {
                width = if (widthOfReplay > widthOfResponse) {
                    LayoutParams.WRAP_CONTENT
                } else {
                    LayoutParams.MATCH_PARENT
                }
            }
        }
    }
}