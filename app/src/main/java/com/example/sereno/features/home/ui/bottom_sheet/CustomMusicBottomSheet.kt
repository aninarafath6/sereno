package com.example.sereno.features.home.ui.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.BuyPremiumBottomSheetBinding
import com.example.sereno.databinding.MusicMixBottomSheetBinding
import com.example.sereno.features.home.ui.adapters.CustomMusicBottomSheetAdapter
import com.example.sereno.features.home.ui.adapters.PremiumBottomSheetAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomMusicBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: MusicMixBottomSheetBinding
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    private val adapter = CustomMusicBottomSheetAdapter(listOf("", "", "", "", "", ""))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MusicMixBottomSheetBinding.inflate(inflater, container, false)
        dialog?.window?.setDimAmount(0f)
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        initListeners()
        initRv()
        return binding.root
    }

    private fun initRv() {
        binding.rv.adapter = adapter
        binding.rv.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rv.layoutManager = LinearLayoutManager(context)
    }

    private fun initListeners() {
        binding.close.onClickWithHaptics {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.root.doOnLayout {
            val bottomSheet = binding.root.parent as View
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior?.addBottomSheetCallback(bottomSheetCallback)
            bottomSheetBehavior?.skipCollapsed = true
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED

            val initialAlpha =
                if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) 1f else 0.5f
            dialog?.window?.setDimAmount(initialAlpha)
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val alpha = ((slideOffset + 1) / 2) * 1f
            dialog?.window?.setDimAmount(alpha.coerceIn(0f, .8f))
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetBehavior?.removeBottomSheetCallback(bottomSheetCallback)
    }
}
