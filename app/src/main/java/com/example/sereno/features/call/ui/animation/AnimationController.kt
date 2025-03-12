package com.example.sereno.features.call.ui.animation

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat

class AnimationController(
    vararg values: Float,
    private val durationMs: Long = 1000,
    repeat: Int = ValueAnimator.INFINITE
) {
    private val valueAnimator = ValueAnimator.ofFloat(*values).apply {
        repeatCount = repeat
        repeatMode = ValueAnimator.REVERSE
        duration = durationMs
        interpolator = LinearInterpolator()
    }

    fun play(
        onComplete: () -> Unit = {},
        onProgress: (value: Float) -> Unit = {},
        onRepeat: () -> Unit = {}
    ) {
        valueAnimator.apply {
            removeAllUpdateListeners()
            addUpdateListener {
                onProgress(it.animatedValue as Float)
            }
            doOnRepeat {
                onRepeat()
            }
            doOnEnd {
                onComplete()
            }
            start()
        }
    }

    fun stop() {
        valueAnimator.cancel()
    }

    fun clear() {
        valueAnimator.removeAllUpdateListeners()
    }
}