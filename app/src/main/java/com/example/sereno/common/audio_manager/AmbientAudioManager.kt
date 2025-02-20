import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sereno.R
import kotlinx.coroutines.*

object AmbientAudioManager {
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

    fun toggleMute(context: Context, shouldMute: Boolean) {
        isMute.value = shouldMute

        if (mediaPlayer == null) {
            init(context, setCurrentRes)
        }

        mediaPlayer?.let { player ->
            currentFadeJob?.cancel()

            if (shouldMute) {
                currentFadeJob = fadeVolume(player, MAX_VOLUME, 0f) {
                    player.pause()
                }
            } else {
                player.setVolume(0f, 0f)
                player.start()
                currentFadeJob = fadeVolume(player, 0f, MAX_VOLUME)
            }
        }
    }

    fun toggleMute(context: Context) {
        toggleMute(context, !isMute.value!!)
    }

    fun getMuteStatus(): LiveData<Boolean> = isMute

    private fun fadeVolume(player: MediaPlayer, startVolume: Float, targetVolume: Float, onComplete: (() -> Unit)? = null): Job {
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
