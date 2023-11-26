package com.example.crayonmarket.view.intro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.crayonmarket.databinding.ActivityIntroBinding
import com.example.crayonmarket.view.login.LoginActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            navigateToLoginActivity()
        }, 3000)
    }

    private fun navigateToLoginActivity() {
        val intent = LoginActivity.getIntent(this)
        startActivity(intent)
        finish()
    }
}