package com.example.sereno.features.home.ui.view_holders

import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.CustomAudioItemBinding
import com.example.sereno.R
import com.example.sereno.features.home.domain.model.CustomAudio
import com.example.sereno.features.home.ui.view_model.AudioViewmodel

class CustomAudioVH(
    private val binding: CustomAudioItemBinding,
    private val audioVm: AudioViewmodel
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: CustomAudio) {
        binding.name.text = item.title

        Glide.with(binding.root.context)
            .load(item.previewImageUrl)
            .into(binding.idPreview)

        binding.root.alpha = if (item.isPlaying) 1f else .3f
        binding.icVolume.setImageResource(
            if (item.isPlaying) {
                R.drawable.ic_volume_on
            } else {
                R.drawable.ic_volume_off
            }
        )

        binding.seekbar.progress = item.volumeLevel
        binding.seekbar.isEnabled = item.isPlaying

        binding.seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let { audioVm.updateVolume(item.copy(volumeLevel = seekBar.progress)) }
                }
            },
        )

        binding.root.onClickWithHaptics {
            audioVm.updatePlayingForCustomAudios(item.copy(isPlaying = !item.isPlaying))
        }
    }
}