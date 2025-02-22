package com.example.sereno.home

import AmbientAudioManager
import BottomSheetDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityHomeBinding
import com.example.sereno.home.adapters.ArticlesAdapter
import com.example.sereno.home.item_decorator.ArticlesPaddingItemDecoration
import com.example.sereno.home.view_mdel.HomeViewModel

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels()
    private val articlesAdapter = ArticlesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AmbientAudioManager.init(this, R.raw.calm_ambient)

        initListeners()
        initObservers()
        initArticlesRecyclerView()
    }

    private fun initListeners() {
        binding.homeHeading.volumeOnOff.muteButton.onClickWithHaptics {
            AmbientAudioManager.toggleMute(this)
        }
        binding.homeFeelingCard.feeling.onClickWithHaptics {
            showBottomSheet()
        }
        binding.homeWeekScoreCard.root.onClickWithHaptics { }
        binding.homeTherapyCard.chat.onClickWithHaptics { }
        binding.homeTherapyCard.call.onClickWithHaptics { }
        binding.homePremiumCard.root.onClickWithHaptics { }
        binding.homeAmbientModeCard.focus.onClickWithHaptics { }
        binding.homeAmbientModeCard.meditate.onClickWithHaptics { }
        binding.homeAmbientModeCard.deepSleep.onClickWithHaptics { }
    }

    private fun initObservers() {
        AmbientAudioManager.getMuteStatus().observe(this) { isMuted ->
            val iconRes = if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on
            binding.homeHeading.volumeOnOff.ivMuteUnMute.setImageResource(iconRes)
        }
    }

    private fun initArticlesRecyclerView() {
        binding.homeArticlesCard.articles.adapter = articlesAdapter
        binding.homeArticlesCard.articles.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        articlesAdapter.setArticles(vm.getArticles())
        binding.homeArticlesCard.articles.addItemDecoration(
            ArticlesPaddingItemDecoration(
                resources.getDimensionPixelSize(R.dimen.spacing_24dp)
            )
        )
    }

    private fun showBottomSheet() {
        val bottomSheet =
            BottomSheetDialog()
        bottomSheet.show(
            supportFragmentManager,
            "ModalBottomSheet"
        )
    }

    override fun onResume() {
        super.onResume()
        AmbientAudioManager.toggleMute(this, shouldMute = false)
    }

    override fun onPause() {
        super.onPause()
        AmbientAudioManager.toggleMute(this, shouldMute = true)
    }
}