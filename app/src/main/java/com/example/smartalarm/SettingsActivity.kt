package com.example.smartalarm

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import android.content.Intent


class SettingsActivity : AppCompatActivity() {

    private lateinit var etCity: EditText
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSave: Button
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE)

        etCity = findViewById(R.id.etCity)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnSave = findViewById(R.id.btnSave)

        setupLanguageSpinner()

        btnSave.setOnClickListener {
            saveSettings()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = AppSettings(newBase).language
        val wrapped = LocaleManager.wrapContext(newBase, lang)
        super.attachBaseContext(wrapped)
    }

    private fun loadSettings() {
        val city = sharedPreferences.getString("city", "Helsinki") ?: "Helsinki"
        val language = sharedPreferences.getString("language", "en") ?: "en"

        etCity.setText(city)

        val position = when (language) {
            "en" -> 0
            "ru" -> 1
            "fi" -> 2
            else -> 0
        }
        spinnerLanguage.setSelection(position)
    }

    private fun setupLanguageSpinner() {
        val languages = listOf(
            getString(R.string.lang_english),
            getString(R.string.lang_russian),
            getString(R.string.lang_finnish)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter
        loadSettings()
    }

    private fun saveSettings() {
        val city = etCity.text.toString().trim()
        val selectedLanguage = spinnerLanguage.selectedItemPosition

        if (city.isEmpty()) {
            Toast.makeText(this, getString(R.string.settings_error_empty_city), Toast.LENGTH_SHORT).show()
            return
        }

        val oldLanguage = sharedPreferences.getString("language", "en") ?: "en"

        val languageCode = when (selectedLanguage) {
            0 -> "en"
            1 -> "ru"
            2 -> "fi"
            else -> "en"
        }
        sharedPreferences.edit {
            putString("city", city)
            putString("language", languageCode)
        }

        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()


        if (oldLanguage != languageCode) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }
}
