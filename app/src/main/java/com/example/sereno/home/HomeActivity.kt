package com.example.sereno.home

import AmbientAudioManager
import BottomSheetDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

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
    }

    private fun initListeners() {
        binding.homeHeading.volumeOnOff.muteButton.onClickWithHaptics {
            AmbientAudioManager.toggleMute(this)
        }
        binding.homeFeelingCard.feeling.onClickWithHaptics {
            showBottomSheet()
        }
        binding.homeTherapyCard.chat.onClickWithHaptics { }
        binding.homeTherapyCard.call.onClickWithHaptics { }
        binding.homePremiumCard.root.onClickWithHaptics { }
    }

    private fun initObservers() {
        AmbientAudioManager.getMuteStatus().observe(this) { isMuted ->
            val iconRes = if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on
            binding.homeHeading.volumeOnOff.ivMuteUnMute.setImageResource(iconRes)
        }
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