package com.example.sereno.features.home.ui.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.MusicMixBottomSheetBinding
import com.example.sereno.features.home.ui.adapters.AudioItemsBottomSheetAdapter
import com.example.sereno.features.home.ui.model.BottomSheetModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AudioListBottomSheet(private val lifecycleOwner: LifecycleOwner, val data: BottomSheetModel) :
    BottomSheetDialogFragment() {

    private lateinit var binding: MusicMixBottomSheetBinding
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private val adapter = AudioItemsBottomSheetAdapter(data.audioVm)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.decorView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBars.top, 0, 0)
                insets
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = MusicMixBottomSheetBinding.inflate(inflater, container, false)
        dialog?.window?.setDimAmount(0f)
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        data.getAudios()
        initRv()
        initUi()
        initObserver()
        initListeners()
        return binding.root
    }

    private fun initObserver() {
        data.audioVm.getLoading.observe(lifecycleOwner) {
            binding.rv.isVisible = !it
            binding.loading.isVisible = it
        }
        data.audioVm.audios.observe(lifecycleOwner) {
            adapter.setAudios(it)
        }
    }

    private fun initUi() {
        binding.title.text = data.title
        binding.subTitle.text = data.subtitle
    }

    private fun initRv() {
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(context)
    }

    private fun initListeners() {
        binding.close.onClickWithHaptics {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onStart() {
        super.onStart()
        binding.root.doOnLayout {
            val bottomSheet = binding.root.parent as View
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior?.addBottomSheetCallback(bottomSheetCallback)
            bottomSheetBehavior?.skipCollapsed = true
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

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
