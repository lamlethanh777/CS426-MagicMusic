package com.example.cs426_magicmusic.ui.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cs426_magicmusic.ui.view.main.MainActivity
import com.example.cs426_magicmusic.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Simulate some loading or delay (e.g., loading resources, etc.)
        val splashScreenDuration = 0L // 2 seconds
        window.decorView.postDelayed({
            // After the delay, launch the MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashScreenDuration)
    }
}
