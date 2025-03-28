package com.example.sereno.features.home.ui.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.HomeFeelingBottomSheetLayoutBinding
import com.example.sereno.features.home.ui.adapters.FeelingsAdapter
import com.example.sereno.features.home.ui.item_decorator.GridSpacingItemDecoration
import com.example.sereno.features.home.domain.model.FeelingItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MoodCheckInBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: HomeFeelingBottomSheetLayoutBinding
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var selectedFeeling: FeelingItem? = null

    private val feelingsList = mutableListOf(
        FeelingItem("Calm", R.drawable.ic_calm),
        FeelingItem("Sad", R.drawable.ic_sad),
        FeelingItem("Tired", R.drawable.ic_tierd),
        FeelingItem("Anxious", R.drawable.ic_anxious),
        FeelingItem("Panicked", R.drawable.ic_panicke),
        FeelingItem("Unsure", R.drawable.ic_unsure)
    )
    private val adapter = FeelingsAdapter(feelingsList) {
        if (!::binding.isInitialized) return@FeelingsAdapter
        binding.continueButton.isEnabled = true
        selectedFeeling = it
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFeelingBottomSheetLayoutBinding.inflate(inflater, container, false)
        dialog?.window?.setDimAmount(0f)
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding.continueButton.isEnabled = false
        initListeners()
        initRv()
        return binding.root
    }

    private fun initRv() {
        val horizontalSpacing = resources.getDimensionPixelSize(R.dimen.spacing_20dp)
        val verticalSpacing = resources.getDimensionPixelSize(R.dimen.spacing_20dp)
        binding.feelingsRecyclerView.adapter = adapter
        binding.feelingsRecyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        binding.feelingsRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.feelingsRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                horizontalSpacing,
                verticalSpacing
            )
        )

    }

    private fun initListeners() {
        binding.close.onClickWithHaptics {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.continueButton.onClickWithHaptics {
            if (selectedFeeling == null) {
                Toast.makeText(context, "Please select a feeling", Toast.LENGTH_SHORT).show()
            } else {
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.root.doOnLayout {
            val bottomSheet = binding.root.parent as View
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior?.addBottomSheetCallback(bottomSheetCallback)

            val initialAlpha =
                if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) 1f else 0.5f
            dialog?.window?.setDimAmount(initialAlpha)
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val alpha = ((slideOffset + 1) / 2) * 1f
            dialog?.window?.setDimAmount(alpha)
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
