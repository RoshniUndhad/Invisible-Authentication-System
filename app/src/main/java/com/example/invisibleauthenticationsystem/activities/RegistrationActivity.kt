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
import com.example.invisibleauthenticationsystem.databinding.ActivityRegistrationBinding
import com.example.invisibleauthenticationsystem.models.User
import com.example.invisibleauthenticationsystem.utils.ThemeManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt


class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
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
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupThemeAndAnimations()

        dbHelper = DatabaseHelper(this)

        setupBehaviorTracking()
        checkBiometricAvailability()


        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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

        binding.etFullName.setOnKeyListener(keyListener)
        binding.etEmail.setOnKeyListener(keyListener)
        binding.etPhone.setOnKeyListener(keyListener)
        binding.etPassword.setOnKeyListener(keyListener)
        binding.etCalcPin.setOnKeyListener(keyListener)

        val touchListener = android.view.View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                totalTouchPressure += event.pressure
                touchCount++
            }
            false
        }

        binding.etFullName.setOnTouchListener(touchListener)
        binding.etEmail.setOnTouchListener(touchListener)
        binding.etPhone.setOnTouchListener(touchListener)
        binding.etPassword.setOnTouchListener(touchListener)
        binding.etCalcPin.setOnTouchListener(touchListener)
        binding.root.setOnTouchListener(touchListener)
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.switchBiometric.isEnabled = true
            }
            else -> {
                binding.switchBiometric.isEnabled = false
                binding.switchBiometric.text = "Biometrics Not Available"
            }
        }
    }


    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val calcPin = binding.etCalcPin.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || calcPin.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.checkUserExists(email, phone)) {
            Toast.makeText(this, "Already Registered? Login.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val avgDwell = if (dwellCount > 0) totalDwellTime.toDouble() / dwellCount else 0.0
        val avgFlight = if (flightCount > 0) totalFlightTime.toDouble() / flightCount else 0.0
        val avgTouchPressure = if (touchCount > 0) (totalTouchPressure / touchCount).toDouble() else 0.0

        val user = User(
            fullName = fullName,
            email = email,
            phone = phone,
            calculatorPin = calcPin,
            avgDwellTime = avgDwell,
            avgFlightTime = avgFlight,
            touchPressure = avgTouchPressure,
            isBiometricEnabled = binding.switchBiometric.isChecked
        )


        val success = dbHelper.registerUser(user, password)
        if (success) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        } else {
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
