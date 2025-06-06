package com.example.sereno.common.extensions

import android.view.HapticFeedbackConstants
import android.view.View

fun View.onClickWithHaptics(onClick: (() -> Unit)?) {
    if (onClick == null) {
        setOnClickListener(null)
        return
    }

    isSoundEffectsEnabled = false
    setOnClickListener {
        it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        onClick.invoke()
    }
}