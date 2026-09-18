package com.example.invisibleauthenticationsystem.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.invisibleauthenticationsystem.R
import com.example.invisibleauthenticationsystem.database.DatabaseHelper
import com.example.invisibleauthenticationsystem.databinding.ActivityLoginBinding
import com.example.invisibleauthenticationsystem.utils.ThemeManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

import kotlin.math.abs

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    // New Behavior Tracking Variables for Dwell and Flight Time
    private var totalDwellTime = 0L
    private var dwellCount = 0
    private var totalFlightTime = 0L
    private var flightCount = 0
    
    private var keyPressTime = 0L
    private var keyReleaseTime = 0L

    private var totalTouchPressure = 0f
    private var touchCount = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeAndAnimations()

        dbHelper = DatabaseHelper(this)
        setupBehaviorTracking()
        setupEmailWatcher()
        setupBiometricLogin()


        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    private fun setupThemeAndAnimations() {
        val themeManager = ThemeManager(this)
        val isDark = themeManager.isDarkMode()

        val toggleIcon = if (isDark) R.drawable.ic_sun else R.drawable.ic_moon
        binding.ivThemeToggle.setImageResource(toggleIcon)

        binding.ivThemeToggle.setOnClickListener {
            val newIsDark = !themeManager.isDarkMode()
            themeManager.setDarkMode(newIsDark)
            recreate()
        }

        binding.root.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.tvTitle.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBehaviorTracking() {
        val keyListener = android.view.View.OnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                val currentTime = System.currentTimeMillis()
                
                // Flight Time: time from LAST release to THIS press
                if (keyReleaseTime != 0L) {
                    val flight = currentTime - keyReleaseTime
                    if (flight in 10..2000) { // Filter out unrealistic speeds or long pauses
                        totalFlightTime += flight
                        flightCount++
                    }
                }
                keyPressTime = currentTime
            } else if (event.action == android.view.KeyEvent.ACTION_UP) {
                val currentTime = System.currentTimeMillis()
                keyReleaseTime = currentTime
                
                // Dwell Time: time from THIS press to THIS release
                if (keyPressTime != 0L) {
                    val dwell = currentTime - keyPressTime
                    if (dwell in 10..2000) {
                        totalDwellTime += dwell
                        dwellCount++
                    }
                }
            }
            false
        }

        binding.etEmail.setOnKeyListener(keyListener)

        val touchListener = android.view.View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                totalTouchPressure += event.pressure
                touchCount++
            }
            false
        }

        binding.etEmail.setOnTouchListener(touchListener)
        binding.root.setOnTouchListener(touchListener)
    }

    private fun setupEmailWatcher() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isNotEmpty()) {
                    val user = dbHelper.getUser(email)
                    if (user != null && user.isBiometricEnabled) {
                        binding.btnBiometricLogin.visibility = android.view.View.VISIBLE
                    } else {
                        binding.btnBiometricLogin.visibility = android.view.View.GONE
                    }
                } else {
                    binding.btnBiometricLogin.visibility = android.view.View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBiometricLogin() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val email = binding.etEmail.text.toString().trim()
                    val user = dbHelper.getUser(email)
                    if (user != null) {
                        Toast.makeText(applicationContext, "Biometric Auth Success!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.putExtra("BEHAVIOR_SCORE", 100) // Biometric is trusted 100%
                        intent.putExtra("USER_NAME", user.fullName)
                        intent.putExtra("USER_EMAIL", user.email)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        finish()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use Email")
            .build()

        binding.btnBiometricLogin.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }


    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        val user = dbHelper.getUser(email)
        if (user == null) {
            Toast.makeText(this, "User not found, please register", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDwell = if (dwellCount > 0) totalDwellTime.toDouble() / dwellCount else 0.0
        val currentFlight = if (flightCount > 0) totalFlightTime.toDouble() / flightCount else 0.0
        val currentTouchPressure = if (touchCount > 0) (totalTouchPressure / touchCount).toDouble() else 0.0

        val dwellSimilarity = calculateSimilarity(currentDwell, user.avgDwellTime)
        val flightSimilarity = calculateSimilarity(currentFlight, user.avgFlightTime)
        val touchSimilarity = calculateSimilarity(currentTouchPressure, user.touchPressure)

        val behaviorScore = ((dwellSimilarity + flightSimilarity + touchSimilarity) / 3.0).toInt()
        
        // Log for debugging (visible to developer in Logcat)
        println("DEBUG_AUTH: Dwell=$dwellSimilarity, Flight=$flightSimilarity, Touch=$touchSimilarity, Total=$behaviorScore")


        if (behaviorScore >= 85) {
            Toast.makeText(this, "Invisible Auth Success!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("BEHAVIOR_SCORE", behaviorScore)
            intent.putExtra("USER_NAME", user.fullName)
            intent.putExtra("USER_EMAIL", user.email)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        } else if (behaviorScore > 69) {
            Toast.makeText(this, "Unusual Behavior Detected. OTP Required.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, OtpActivity::class.java)
            intent.putExtra("BEHAVIOR_SCORE", behaviorScore)
            intent.putExtra("EMAIL", email)
            intent.putExtra("PHONE", user.phone)
            intent.putExtra("NAME", user.fullName)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        } else {
            Toast.makeText(this, "Access Blocked! Exiting App.", Toast.LENGTH_LONG).show()
            finishAffinity()
        }
    }

    private fun calculateSimilarity(current: Double, registered: Double): Double {
        if (registered <= 0.0) return 100.0 
        val difference = abs(current - registered)
        
        // Use a more lenient non-linear formula: similarity = 100 / (1 + (difference/registered))
        // This is much more stable than linear percentage for low-value metrics
        val similarity = (100.0 / (1.0 + (difference / registered)))
        
        return similarity.coerceIn(0.0, 100.0)
    }

}
