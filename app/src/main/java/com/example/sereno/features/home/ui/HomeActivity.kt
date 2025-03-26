package com.example.sereno.features.home.ui

import BottomSheetDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityHomeBinding
import com.example.sereno.features.chat.ui.ChatActivity
import com.example.sereno.features.home.ui.adapters.ArticlesAdapter
import com.example.sereno.features.home.ui.item_decorator.ArticlesPaddingItemDecoration
import com.example.sereno.features.home.domain.HomeViewModel
import com.example.sereno.features.home.domain.model.AmbientItem
import com.example.sereno.features.home.ui.bottom_sheet.BuyPremiumBottomSheet
import com.example.sereno.features.home.ui.bottom_sheet.CustomMusicBottomSheet

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels()
    private val articlesAdapter = ArticlesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        enableEdgeToEdge()
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
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
            val bottomSheet = CustomMusicBottomSheet()
            bottomSheet.show(
                supportFragmentManager,
                "ModalBottomSheet"
            )
        }
        binding.homeAmbientModeCard.deepSleep.onClickWithHaptics {
            vm.onAmbientCardClicked(this, AmbientItem.SLEEP)
        }
    }

    private fun initObservers() {
        vm.muteIconVisibility.observe(this) {
            val iconRes = if (it) R.drawable.ic_volume_off else R.drawable.ic_volume_on
            binding.homeHeading.volumeOnOff.ivMuteUnMute.setImageResource(iconRes)
            resetMusicPlayStateUi()
            if (!it) {
                when (vm.getSelected()) {
                    AmbientItem.CUSTOM -> {
                        setPlayingMusicItem(
                            binding.homeAmbientModeCard.customLoading,
                            binding.homeAmbientModeCard.icCustom
                        )
                    }

                    AmbientItem.FOCUS -> {
                        setPlayingMusicItem(
                            binding.homeAmbientModeCard.focusLoading,
                            binding.homeAmbientModeCard.icFocus
                        )
                    }

                    AmbientItem.SLEEP -> {
                        setPlayingMusicItem(
                            binding.homeAmbientModeCard.sleepLoading,
                            binding.homeAmbientModeCard.icSleep
                        )
                    }

                    AmbientItem.NONE -> {}
                }
            }
        }
    }

    private fun setPlayingMusicItem(loadingView: View, icon: View) {
        loadingView.isVisible = true
        icon.isVisible = false
    }

    private fun resetMusicPlayStateUi() {
        binding.homeAmbientModeCard.focusLoading.isVisible = false
        binding.homeAmbientModeCard.customLoading.isVisible = false
        binding.homeAmbientModeCard.sleepLoading.isVisible = false
        binding.homeAmbientModeCard.icSleep.isVisible = true
        binding.homeAmbientModeCard.icFocus.isVisible = true
        binding.homeAmbientModeCard.icCustom.isVisible = true
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