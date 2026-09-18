package com.example.invisibleauthenticationsystem.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.invisibleauthenticationsystem.R
import com.example.invisibleauthenticationsystem.utils.ThemeManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize theme before splash is shown
        ThemeManager(this).isDarkMode()

        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        val tvAppName = findViewById<TextView>(R.id.tvAppName)

        // Simple fade-in animation for the logo and text
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        ivLogo.startAnimation(fadeIn)
        tvAppName.startAnimation(fadeIn)

        // Delay for 3 seconds (3000ms), then launch Calculator
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, CalculatorActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}
