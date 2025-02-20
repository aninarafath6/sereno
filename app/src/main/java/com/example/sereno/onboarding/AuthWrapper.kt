package com.example.sereno.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sereno.R
import com.example.sereno.common.supabase.SupabaseManager
import com.example.sereno.databinding.ActivityAuthWrapperBinding
import com.example.sereno.home.HomeActivity
import com.example.sereno.onboarding.views.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthWrapper : AppCompatActivity() {
    private lateinit var binding: ActivityAuthWrapperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthWrapperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        SupabaseManager.init()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        redirect()
    }


    private fun redirect() {
        lifecycleScope.launch {
            val intent: Intent = if (SupabaseManager.isUserAuthenticated()) {
                Intent(this@AuthWrapper, HomeActivity::class.java)
            } else {
                Intent(this@AuthWrapper, MainActivity::class.java)
            }
            overridePendingTransition(0, 0)
            startActivity(intent)
            finish()
        }
    }
}