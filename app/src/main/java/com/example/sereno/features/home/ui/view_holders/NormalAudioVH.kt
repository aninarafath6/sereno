package com.example.sereno.features.home.ui.view_holders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sereno.databinding.NormalAudioItemBinding
import com.example.sereno.features.home.domain.model.NormalAudio

class NormalAudioVH(private val binding: NormalAudioItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: NormalAudio) {
        binding.name.text = item.title
        binding.subtitle.text = item.subtitle
        Glide.with(binding.root.context)
            .load(item.previewImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.idPreview)
    }
}