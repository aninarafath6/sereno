package com.example.sereno.features.home.ui

import BottomSheetDialog
import android.content.Intent
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
import com.example.sereno.features.chat.ui.ChatActivity
import com.example.sereno.features.home.ui.adapters.ArticlesAdapter
import com.example.sereno.features.home.ui.item_decorator.ArticlesPaddingItemDecoration
import com.example.sereno.features.home.domain.HomeViewModel
import com.example.sereno.features.home.domain.model.AmbientItem

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels()
    private val articlesAdapter = ArticlesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupWindowInsets()
        vm.init(this, this)
        initListeners()
        initObservers()
        initArticlesRecyclerView()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    private fun initListeners() {
        binding.homeHeading.volumeOnOff.muteButton.onClickWithHaptics {
            vm.toggleMute()
        }
        binding.homeFeelingCard.feeling.onClickWithHaptics {
            showBottomSheet()
        }
        binding.homeTherapyCard.chat.onClickWithHaptics {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        binding.homeTherapyCard.call.onClickWithHaptics {
            showPremiumBottomSheet()
        }
        binding.homeAmbientModeCard.focus.onClickWithHaptics {
            vm.onAmbientCardClicked(this, AmbientItem.FOCUS)
        }
        binding.homeAmbientModeCard.custom.onClickWithHaptics {
            vm.onAmbientCardClicked(this, AmbientItem.CUSTOM)
        }
        binding.homeAmbientModeCard.deepSleep.onClickWithHaptics {
            vm.onAmbientCardClicked(this, AmbientItem.SLEEP)
        }
    }

    private fun initObservers() {
        vm.muteIconVisibility.observe(this) {
            val iconRes = if (it) R.drawable.ic_volume_off else R.drawable.ic_volume_on
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
                resources.getDimensionPixelSize(
                    R.dimen.spacing_24dp
                )
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

    private fun showPremiumBottomSheet() {
        val bottomSheet =
            BuyPremiumBottomSheet()
        bottomSheet.show(
            supportFragmentManager,
            "ModalBottomSheet"
        )
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onPause() {
        super.onPause()
        vm.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.onDestroy()
    }
}