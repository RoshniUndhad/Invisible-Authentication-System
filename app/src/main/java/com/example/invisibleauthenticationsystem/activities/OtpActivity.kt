package com.example.invisibleauthenticationsystem.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.invisibleauthenticationsystem.R
import com.example.invisibleauthenticationsystem.database.DatabaseHelper
import com.example.invisibleauthenticationsystem.databinding.ActivityOtpBinding
import com.example.invisibleauthenticationsystem.utils.ThemeManager

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var dbHelper: DatabaseHelper
    private var generatedOtp: String = ""
    private var targetPhone: String = ""
    private var targetEmail: String = ""
    private var expectedCalcPin: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            sendRealSms()
        } else {
            Toast.makeText(this, "SMS Permission Denied. Could not send code.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeAndAnimations()
        dbHelper = DatabaseHelper(this)

        val behaviorScore = intent.getIntExtra("BEHAVIOR_SCORE", 0)
        targetEmail = intent.getStringExtra("EMAIL") ?: ""
        targetPhone = intent.getStringExtra("PHONE") ?: ""
        
        val user = dbHelper.getUser(targetEmail)
        expectedCalcPin = user?.calculatorPin ?: ""
        
        generatedOtp = (1000..9999).random().toString()
        Toast.makeText(this, "Processing verification for $targetEmail...", Toast.LENGTH_SHORT).show()
        
        // TESTING FALLBACK: Show the OTP on screen in case SMS fails on real device networks
        Toast.makeText(this, "TEST OTP: $generatedOtp", Toast.LENGTH_LONG).show()

        sendOtpToEmail()
        checkAndRequestSmsPermission()

        binding.btnVerifyOtp.setOnClickListener {
            val otpCode = binding.etOtp.text.toString()
            val pinCode = binding.etCalcPin.text.toString()

            if (otpCode == generatedOtp && pinCode == expectedCalcPin) {
                Toast.makeText(this, "Verification Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("BEHAVIOR_SCORE", behaviorScore)
                
                val userName = getIntent().getStringExtra("NAME") ?: "Welcome, User"
                intent.putExtra("USER_NAME", userName)
                intent.putExtra("USER_EMAIL", targetEmail)

                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            } else if (otpCode != generatedOtp) {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid Calculator PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendRealSms()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
        }
    }

    private fun sendRealSms() {
        try {
            if (targetPhone.isNotEmpty()) {
                val smsManager: SmsManager = this.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(targetPhone, null, "Your Invisible Authenticaton Vault OTP code is: $generatedOtp", null, null)
                Toast.makeText(this, "OTP SMS sent to $targetPhone", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to send SMS OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOtpToEmail() {
        if (targetEmail.isNotEmpty()) {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(targetEmail))
                putExtra(Intent.EXTRA_SUBJECT, "Your Vault OTP Code")
                putExtra(Intent.EXTRA_TEXT, "Hello,\n\nYour one-time password (OTP) to access the Invisible Authentication Vault is: $generatedOtp\n\nDo not share this code with anyone.")
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Send OTP via Email"))
            } catch (e: Exception) {
                Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show()
            }
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
}
