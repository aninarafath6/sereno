package com.example.sereno.features.home.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.databinding.CustomAudioItemBinding
import com.example.sereno.databinding.NormalAudioItemBinding
import com.example.sereno.features.home.domain.model.BaseAudio
import com.example.sereno.features.home.domain.model.CustomAudio
import com.example.sereno.features.home.domain.model.NormalAudio
import com.example.sereno.features.home.ui.view_holders.CustomAudioVH
import com.example.sereno.features.home.ui.view_holders.NormalAudioVH
import com.example.sereno.features.home.ui.view_model.AudioViewmodel

class AudioItemsBottomSheetAdapter(private val audioVm: AudioViewmodel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val audios = mutableListOf<BaseAudio>()

    companion object {
        private const val CUSTOM_AUDIO_ITEM = 1
        private const val NORMAL_AUDIO_ITEM = 2
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return audios[position].id
    }

    override fun getItemViewType(position: Int): Int {
        return if (audios.first() is NormalAudio) NORMAL_AUDIO_ITEM else CUSTOM_AUDIO_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = if (viewType == CUSTOM_AUDIO_ITEM) {
            CustomAudioVH(
                CustomAudioItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                audioVm
            )
        } else {
            NormalAudioVH(
                NormalAudioItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val audio = audios[position]
        if (holder is CustomAudioVH) {
            holder.bind(audio as CustomAudio)
        } else if (holder is NormalAudioVH) {
            holder.bind(audio as NormalAudio)
        }
    }

    override fun getItemCount(): Int = audios.size

    fun setAudios(audios: List<BaseAudio>) {
        this.audios.clear()
        this.audios.addAll(audios)
        notifyDataSetChanged()
    }
}
