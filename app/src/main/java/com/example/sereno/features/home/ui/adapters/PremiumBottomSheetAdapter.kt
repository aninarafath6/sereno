package com.example.sereno.features.home.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.FeelingBottomSheetItemBinding
import com.example.sereno.databinding.PremiumCardItemBinding
import com.example.sereno.features.home.domain.model.FeelingItem

class PremiumBottomSheetAdapter(private val items: List<String>) :
    RecyclerView.Adapter<PremiumBottomSheetAdapter.VH>() {

    inner class VH(private val binding: PremiumCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.root.text = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = PremiumCardItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
