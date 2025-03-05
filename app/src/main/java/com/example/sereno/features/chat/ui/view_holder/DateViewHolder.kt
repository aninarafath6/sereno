package com.example.sereno.features.chat.ui.view_holder

import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.databinding.DateItemBinding

class DateViewHolder(private val binding: DateItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(formatedDate: String) {
        binding.date.text = formatedDate
    }
}