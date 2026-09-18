package com.example.invisibleauthenticationsystem.activities

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.invisibleauthenticationsystem.R
import com.example.invisibleauthenticationsystem.adapters.DocumentAdapter
import com.example.invisibleauthenticationsystem.database.DatabaseHelper
import com.example.invisibleauthenticationsystem.databinding.ActivityDashboardBinding
import com.example.invisibleauthenticationsystem.models.Document
import com.example.invisibleauthenticationsystem.utils.CryptoManager
import com.example.invisibleauthenticationsystem.utils.ThemeManager
import java.io.File
import java.io.FileOutputStream

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val docList = mutableListOf<Document>()
    private lateinit var docAdapter: DocumentAdapter
    private lateinit var dbHelper: DatabaseHelper
    private val cryptoManager = CryptoManager()
    private var currentUserEmail: String = ""

    private val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        Toast.makeText(this, "Session time out due to inactivity.", Toast.LENGTH_LONG).show()
        finishAffinity()
    }
    private val TIMEOUT_IN_MS = 3 * 60 * 1000L // 3 minutes

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        for (uri in uris) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Ignore exception if permission is already granted or can't be taken
            }
            val fileName = getFileName(uri)
            
            // Generate a secure internal cache file
            val encryptedFile = File(cacheDir, "enc_${System.currentTimeMillis()}_$fileName")
            try {
                // Encrypt payload to internal storage using KeyStore
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    FileOutputStream(encryptedFile).use { outputStream ->
                        cryptoManager.encrypt(bytes, outputStream)
                    }
                }

                val doc = Document(
                    title = fileName,
                    uriString = encryptedFile.absolutePath, // Saving internal encrypted path instead of public URI
                    mimeType = contentResolver.getType(uri) ?: "*/*"
                )
                
                if (currentUserEmail.isNotEmpty()) {
                    val dbResult = dbHelper.insertDocument(currentUserEmail, doc)
                    if (dbResult) {
                        docList.add(doc)
                        Toast.makeText(this, "Encrypted and stored: $fileName", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save ${doc.title} to Database", Toast.LENGTH_SHORT).show()
                    }
                } else {
                     docList.add(doc)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Encryption Failed for $fileName", Toast.LENGTH_SHORT).show()
            }
        }
        if (uris.isNotEmpty()) {
            docAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent Screenshots and Screen Recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        startSessionTimeout()
        
        setupThemeAndAnimations()

        val behaviorScore = intent.getIntExtra("BEHAVIOR_SCORE", 100)
        setupScoreIndicator(behaviorScore)

        val userName = intent.getStringExtra("USER_NAME") ?: "Welcome, User"
        currentUserEmail = intent.getStringExtra("USER_EMAIL") ?: "user@example.com"
        
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = currentUserEmail

        dbHelper = com.example.invisibleauthenticationsystem.database.DatabaseHelper(this)
        
        loadUserDocuments()

        docAdapter = DocumentAdapter(this, docList) { doc, position ->
            AlertDialog.Builder(this)
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to permanently delete '${doc.title}'?")
                .setPositiveButton("Delete") { _, _ ->
                    val deleted = dbHelper.deleteDocument(doc.id)
                    if (deleted) {
                        try {
                            val internalFile = File(doc.uriString)
                            if (internalFile.exists()) {
                                internalFile.delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        docList.removeAt(position)
                        docAdapter.notifyItemRemoved(position)
                        Toast.makeText(this, "Document deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to delete document", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.rvDocuments.layoutManager = LinearLayoutManager(this)
        binding.rvDocuments.adapter = docAdapter

        binding.btnUpload.setOnClickListener {
            // Allows all files
            filePickerLauncher.launch("*/*")
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Toast.makeText(this, "Logged out safely", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetSessionTimeout()
    }

    private fun startSessionTimeout() {
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_IN_MS)
    }

    private fun resetSessionTimeout() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        startSessionTimeout()
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutHandler.removeCallbacks(timeoutRunnable)
    }

    private fun loadUserDocuments() {
        if (currentUserEmail.isNotEmpty() && currentUserEmail != "user@example.com") {
            val savedDocs = dbHelper.getDocumentsForUser(currentUserEmail)
            docList.clear()
            docList.addAll(savedDocs)
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

    private fun setupScoreIndicator(score: Int) {
        binding.tvScoreValue.text = score.toString()
        binding.cpScoreRing.progress = score

        val colorRes: Int
        val riskText: String

        if (score >= 85) {
            colorRes = R.color.safeText // Assuming this is Green
            riskText = "Status: Low Risk"
        } else if (score > 69) {
            colorRes = R.color.warningText // Assuming this is Orange
            riskText = "Status: Medium Risk"
        } else {
            // High risk (<= 69) should never logically reach the dashboard, 
            // but we provide a fallback color just for safety.
            colorRes = R.color.dangerText
            riskText = "Status: High Risk (Error State)"
        }

        val dynamicColor = resources.getColor(colorRes, theme)
        binding.tvScoreValue.setTextColor(dynamicColor)
        binding.cpScoreRing.setIndicatorColor(dynamicColor)
        
        binding.tvRiskLevel.text = riskText
        binding.tvRiskLevel.setTextColor(dynamicColor)
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Unknown Document"
    }
}
