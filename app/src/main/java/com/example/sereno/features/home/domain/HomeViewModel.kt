package com.example.sereno.features.home.domain

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sereno.common.audio_manager.AmbientAudioManager
import com.example.sereno.features.home.domain.model.AmbientItem
import com.example.sereno.features.home.domain.model.ArticleModel

class HomeViewModel : ViewModel() {
    private val ambientAudioManager = AmbientAudioManager()
    private var _muteIconVisibility = MutableLiveData(false)
    private var selectedAmbient: AmbientItem = AmbientItem.DEFAULT

    val muteIconVisibility: LiveData<Boolean> = _muteIconVisibility

    fun init(context: Context, lifecycleOwner: LifecycleOwner) {
        ambientAudioManager.getMuteStatus().observe(lifecycleOwner) {
            _muteIconVisibility.value = it
        }
        refreshAmbiance(context)
    }

    fun toggleMute() {
        ambientAudioManager.toggleMute()
    }

    fun getArticles(): List<ArticleModel> {
        return listOf(
            ArticleModel(
                "The Best Ways to Improve Your Sleep Quality",
                "",
                "Today",
                "https://vantage-nutrition.com/wp-content/uploads/2024/09/Why-good-sleep-matters.png"
            ),
            ArticleModel(
                "How Meditation Can Reduce Stress and Anxiety",
                "",
                "Yesterday",
                "https://blog.cdn.level.game/2023/07/front-view-man-meditating-outdoors-yoga-mat.jpg"
            ),
            ArticleModel(
                "The Science Behind Deep Sleep and Its Benefits",
                "",
                "2 days ago",
                "https://akm-img-a-in.tosshub.com/indiatoday/styles/medium_crop_simple/public/2024-01/gettyimages-1134270597-170667a.jpg?VersionId=3vlswnsfRr80XVbe7n7YrOTea3W6w9yx"
            ),
            ArticleModel(
                "Top Relaxation Techniques for a Better Night’s Sleep",
                "",
                "Last week",
                "https://static.toiimg.com/thumb/111637011/111637011.jpg?height=746&width=420&resizemode=76&imgsize=1467765"
            ),
            ArticleModel(
                "Mindfulness Practices to Calm Your Mind",
                "",
                "10 days ago",
                "https://wonderscounseling.com/wp-content/uploads/2012/05/woman-meditation.jpg"
            ),
        )
    }

    fun onAmbientCardClicked(context: Context, ambientItem: AmbientItem) {
        if (ambientItem == selectedAmbient) {
            ambientAudioManager.togglePlayBack()
            return
        }
        selectedAmbient = ambientItem
        refreshAmbiance(context)
    }

    private fun refreshAmbiance(context: Context) {
        when (selectedAmbient) {
            AmbientItem.CUSTOM -> {
            }

            else -> {
                ambientAudioManager.play(context, selectedAmbient.source!!)
                ambientAudioManager.unMute()
            }
        }
    }

    fun onPause() {
        ambientAudioManager.mute()
    }

    fun onResume() {
        ambientAudioManager.unMuteIfPossible()
    }

    fun onDestroy() {
        ambientAudioManager.destroy()
    }
}