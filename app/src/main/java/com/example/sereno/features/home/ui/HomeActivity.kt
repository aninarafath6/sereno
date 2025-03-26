package com.example.sereno.features.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityHomeBinding
import com.example.sereno.features.chat.ui.ChatActivity
import com.example.sereno.features.home.domain.HomeViewModel
import com.example.sereno.features.home.domain.model.Ambiance
import com.example.sereno.features.home.ui.adapters.ArticlesAdapter
import com.example.sereno.features.home.ui.bottom_sheet.AudioListBottomSheet
import com.example.sereno.features.home.ui.bottom_sheet.BuyPremiumBottomSheet
import com.example.sereno.features.home.ui.bottom_sheet.MoodCheckInBottomSheet
import com.example.sereno.features.home.ui.item_decorator.ArticlesPaddingItemDecoration

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels()
    private val articlesAdapter = ArticlesAdapter()
    private val moodCheckInBottomSheet = MoodCheckInBottomSheet()
    private val bottomSheet = BuyPremiumBottomSheet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        enableEdgeToEdge()
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        setContentView(binding.root)
        setupWindowInsets()
        vm.init()
        initListeners()
        initArticlesRecyclerView()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left, systemBars.top, systemBars.right, systemBars.bottom
            )
            insets
        }
    }

    private fun initListeners() {
        binding.homeFeelingCard.feeling.onClickWithHaptics {
            moodCheckInBottomSheet.show(
                supportFragmentManager, "ModalBottomSheet"
            )
        }
        binding.homeTherapyCard.chat.onClickWithHaptics {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        binding.homeTherapyCard.call.onClickWithHaptics {
            bottomSheet.show(supportFragmentManager, "ModalBottomSheet")
        }
        binding.homeAmbientModeCard.focus.onClickWithHaptics {
            showMusicBottomSheet(Ambiance.Focus)
        }
        binding.homeAmbientModeCard.custom.onClickWithHaptics {
            showMusicBottomSheet(Ambiance.Custom)
        }
        binding.homeAmbientModeCard.deepSleep.onClickWithHaptics {
            showMusicBottomSheet(Ambiance.Sleep)
        }

    }

    private fun showMusicBottomSheet(ambiance: Ambiance) {
        val audioBottomSheet = AudioListBottomSheet(vm, ambiance)
        audioBottomSheet.show(supportFragmentManager, "ModalBottomSheet")
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

}