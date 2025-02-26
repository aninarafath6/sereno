package com.example.sereno.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.sereno.R
import com.example.sereno.chat.model.Chat
import com.example.sereno.databinding.ChatBinding

class ChatItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defaultStyleAttr) {

    val binding: ChatBinding = ChatBinding.inflate(LayoutInflater.from(context), this, true)
    private var chatOwner = USER

    companion object {
        private const val USER = 0
        private const val BOT = 1
    }

    init {
        val styles =
            context.obtainStyledAttributes(attrs, R.styleable.ChatType, defaultStyleAttr, 0)
        chatOwner = styles.getInt(R.styleable.ChatType_owner, USER)
        setBackground()
        styles.recycle()
    }

    fun setChatText(chat: Chat) {
        binding.response.text = chat.message
        binding.replayContainer.post {
            adjustReplayContainerConstraints()
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

        binding.root.setBackgroundResource(background)
        binding.replayBg.setBackgroundResource(replayBackground)
    }

    private fun adjustReplayContainerConstraints() {
        binding.replay.post {
            val messageWidth =
                binding.response.width
            val replayWidth =
                binding.replay.width

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.root)

            if (replayWidth >= messageWidth) {
                constraintSet.clear(R.id.replayContainer, ConstraintSet.END)
            } else {
                constraintSet.connect(
                    R.id.replayContainer, ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END
                )
            }
            constraintSet.applyTo(binding.root)
            binding.replayContainer.requestLayout()
        }
    }
}
