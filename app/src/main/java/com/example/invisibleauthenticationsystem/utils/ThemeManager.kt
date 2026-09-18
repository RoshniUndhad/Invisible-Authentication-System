package com.example.invisibleauthenticationsystem.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean {
        // Automatically default to system setting, or false if not supported
        val defaultMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        return prefs.getBoolean("IS_DARK_MODE", defaultMode)
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("IS_DARK_MODE", isDark).apply()
        applyTheme(isDark)
    }

    fun applyTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
