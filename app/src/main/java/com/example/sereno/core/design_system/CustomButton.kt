package com.example.sereno.core.design_system

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.example.sereno.R
import com.example.sereno.databinding.PrimaryButtonBinding

@SuppressLint("CustomViewStyleable")
class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyleAttr: Int = 0
) : LinearLayout(context, attrs, defaultStyleAttr) {

    private var iconRes: Int = 0
    private var text: String = "Continue"
    private var size: Int = NORMAL
    private var iconSize: Float
    private val binding = PrimaryButtonBinding.inflate(LayoutInflater.from(context), this, true)

    companion object {
        private const val SMALL = 0
        private const val NORMAL = 1
    }

    init {
        orientation = HORIZONTAL
        val styles =
            context.obtainStyledAttributes(attrs, R.styleable.ButtonStyle, defaultStyleAttr, 0)

        iconRes = styles.getResourceId(R.styleable.ButtonStyle_icon, 0)
        iconSize = styles.getDimension(
            R.styleable.ButtonStyle_iconSize,
            resources.getDimension(R.dimen.spacing_24dp)
        )
        size = styles.getInt(R.styleable.ButtonStyle_size, NORMAL)
        text = styles.getString(R.styleable.ButtonStyle_text) ?: "Continue"
        styles.recycle()

        refresh()
    }

    private fun refresh() {
        binding.text.text = text
        binding.icon.isVisible = iconRes != 0
        if (iconRes != 0) {
            binding.icon.setImageResource(iconRes)
        }
        binding.root.setBackgroundResource(
            if (size == SMALL) R.drawable.primary_button_bg_small else R.drawable.primary_button_bg
        )
        binding.icon.updateLayoutParams {
            height = iconSize.toInt()
            width = iconSize.toInt()
        }
    }

    override fun setEnabled(isEnabled: Boolean) {
        super.setEnabled(isEnabled)
        binding.root.isEnabled = isEnabled
    }

    fun setText(newText: String) {
        text = newText
        binding.text.text = newText
    }

    fun setIcon(iconResId: Int) {
        iconRes = iconResId
        binding.icon.isVisible = iconRes != 0
        if (iconRes != 0) {
            binding.icon.setImageResource(iconRes)
        }
    }
}
