package com.example.sereno.features.onboarding.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sereno.R
import com.example.sereno.common.supabase.SupabaseManager
import com.example.sereno.databinding.ActivityAuthWrapperBinding
import com.example.sereno.features.home.ui.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthWrapper : AppCompatActivity() {
    private lateinit var binding: ActivityAuthWrapperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthWrapperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent)
        SupabaseManager.init()
        redirect()
    }

    private fun redirect() {
        lifecycleScope.launch {
            delay(FADE_DELAY_MS)
            fadeTitle(true)
            val intent: Intent = if (SupabaseManager.isUserAuthenticated()) {
                Intent(this@AuthWrapper, HomeActivity::class.java)
            } else {
                Intent(this@AuthWrapper, MainActivity::class.java)
            }
            overridePendingTransition(0, 0)

            delay(LOADING_MOCK_MS)
            fadeTitle(false)
            delay(FADE_DELAY_MS)
            startActivity(intent)
            finish()
        }
    }

    private fun fadeTitle(show: Boolean) {
        binding.onboardingTitle.animate().apply {
            duration = FADE_DELAY_MS
            alpha(if (show) .75f else 0f)
        }.start()
    }

    companion object {
        private const val FADE_DELAY_MS = 300L
        private const val LOADING_MOCK_MS = 1500L
    }
}