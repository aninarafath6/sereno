package com.example.sereno.common.extensions

import android.view.HapticFeedbackConstants
import android.view.View

fun View.onClickWithHaptics(onClick: () -> Unit) {
    setOnClickListener {
        onClick.invoke()
        it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}