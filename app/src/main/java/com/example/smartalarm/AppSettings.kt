package com.example.smartalarm

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppSettings(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

    var city: String
        get() = sharedPreferences.getString("city", "Tampere") ?: "Helsinki"
        set(value) = sharedPreferences.edit { putString("city", value) }

    var language: String
        get() = sharedPreferences.getString("language", "en") ?: "en"
        set(value) = sharedPreferences.edit { putString("language", value) }
}
