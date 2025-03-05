package com.example.sereno.features.home.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.FeelingBottomSheetItemBinding
import com.example.sereno.features.home.domain.model.FeelingItem

class FeelingsAdapter(
    private val items: List<FeelingItem>,
    private val onItemClick: (FeelingItem) -> Unit
) : RecyclerView.Adapter<FeelingsAdapter.FeelingViewHolder>() {
    private var selectedItemPosition = -1

    inner class FeelingViewHolder(private val binding: FeelingBottomSheetItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(feeling: FeelingItem, position: Int) {
            binding.feelingText.text = feeling.text
            binding.feelingIcon.setImageResource(feeling.iconResId)
            binding.root.isSelected = feeling.isSelected
            binding.root.isSelected = selectedItemPosition == position

            binding.root.onClickWithHaptics {
                onItemClick(items[position])
                selectedItemPosition = position
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeelingViewHolder {
        val binding = FeelingBottomSheetItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeelingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeelingViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
