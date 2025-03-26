package com.example.sereno.features.home.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.databinding.CustomSoundItemBinding
import com.example.sereno.features.home.domain.model.AudioItem

class AudioItemsBottomSheetAdapter : RecyclerView.Adapter<AudioItemsBottomSheetAdapter.VH>() {
    private val audios = mutableListOf<AudioItem>()
    inner class VH(private val binding: CustomSoundItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.name.text = "khjj"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = CustomSoundItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (audios.isEmpty()) return
        holder.bind()
    }

    override fun getItemCount(): Int = audios.size

    fun setAudios(audios: List<AudioItem>) {
        this.audios.clear()
        this.audios.addAll(audios)
        notifyDataSetChanged()
    }


}
