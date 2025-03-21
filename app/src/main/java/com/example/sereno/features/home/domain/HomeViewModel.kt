package com.example.sereno.features.home.domain

import androidx.lifecycle.ViewModel
import com.example.sereno.R
import com.example.sereno.features.home.domain.model.ArticleModel
import com.example.sereno.features.home.domain.model.FeelingItem

class HomeViewModel : ViewModel() {

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

    fun getLastFeelingEmotion(): FeelingItem {
        return FeelingItem(
            text = "Happy",
            iconResId = R.drawable.ic_happy,
            description = "A Very Pleasant Moment",
            subDescription = "Joyful",
            whatMade = "Self-care"
        )
    }
}