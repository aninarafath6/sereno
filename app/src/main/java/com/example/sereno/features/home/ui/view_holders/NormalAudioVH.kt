package com.example.sereno.features.home.ui.view_holders

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.NormalAudioItemBinding
import com.example.sereno.features.home.domain.model.NormalAudio
import com.example.sereno.features.home.ui.view_model.AudioViewmodel

class NormalAudioVH(
    private val binding: NormalAudioItemBinding,
    private val audioVm: AudioViewmodel
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: NormalAudio) {
        binding.name.text = item.title
        binding.subtitle.text = item.subtitle

        Glide.with(binding.root.context)
            .load(item.previewImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.idPreview)

        binding.playingState.isVisible = item.isPlaying
        binding.icPlayPause.isVisible = !item.isPlaying
        binding.playingState.loop(true)
        binding.playingState.setAnimation(R.raw.lottie_music_playing)

        if (item.isPlaying) {
            binding.playingState.playAnimation()
        } else {
            binding.playingState.cancelAnimation()
        }

        binding.root.onClickWithHaptics {
            audioVm.updateItem(item.copy(isPlaying = !item.isPlaying))
        }
    }
}