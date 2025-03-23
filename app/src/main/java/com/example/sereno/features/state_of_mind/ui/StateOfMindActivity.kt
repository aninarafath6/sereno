package com.example.sereno.features.state_of_mind.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sereno.R
import com.example.sereno.databinding.ActivityStateOfMindBinding

class StateOfMindActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStateOfMindBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStateOfMindBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, StateOfMindActivity::class.java)
            context.startActivity(intent)
        }
    }
}