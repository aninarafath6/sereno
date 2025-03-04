import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sereno.R
import kotlinx.coroutines.*

object AudioManager {
    private val isMute = MutableLiveData(false)
    private var mediaPlayer: MediaPlayer? = null
    private var setCurrentRes = R.raw.rain_ambient
    private const val FADE_DURATION = 800L
    private const val FADE_STEPS = 30
    private const val MAX_VOLUME = 0.5f
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentFadeJob: Job? = null

    @Synchronized
    fun init(context: Context, res: Int = R.raw.rain_ambient) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context.applicationContext, res).apply {
            isLooping = true
            setVolume(0f, 0f)
        }
    }

    fun toggleMute(context: Context, shouldMute: Boolean, shouldFade: Boolean = true) {
        isMute.value = shouldMute

        if (mediaPlayer == null) {
            init(context, setCurrentRes)
        }

        mediaPlayer?.let { player ->
            currentFadeJob?.cancel()

            if (shouldMute) {
                if (shouldFade) {
                    currentFadeJob = fadeVolume(player, MAX_VOLUME, 0f) {
                        player.pause()
                    }
                } else {
                    player.setVolume(0f, 0f)
                    player.pause()
                }
            } else {
                if (shouldFade) {
                    player.setVolume(0f, 0f)
                    player.start()
                    currentFadeJob = fadeVolume(player, 0f, MAX_VOLUME)
                } else {
                    player.setVolume(MAX_VOLUME, MAX_VOLUME)
                    player.start()
                }
            }
        }
    }

    fun toggleMute(context: Context, shouldFade: Boolean = true) {
        toggleMute(context, !isMute.value!!, shouldFade)
    }

    fun getMuteStatus(): LiveData<Boolean> = isMute

    private fun fadeVolume(
        player: MediaPlayer,
        startVolume: Float,
        targetVolume: Float,
        onComplete: (() -> Unit)? = null
    ): Job {
        return scope.launch {
            val stepDelay = FADE_DURATION / FADE_STEPS
            val volumeStep = (targetVolume - startVolume) / FADE_STEPS

            for (i in 0..FADE_STEPS) {
                val newVolume = (startVolume + i * volumeStep).coerceIn(0f, MAX_VOLUME)
                player.setVolume(newVolume, newVolume)
                delay(stepDelay)
            }
            onComplete?.invoke()
        }
    }

}
