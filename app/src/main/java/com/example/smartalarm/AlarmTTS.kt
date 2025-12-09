package com.example.smartalarm

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class AlarmTTS(context: Context, private var language: String = "en") {
    private lateinit var textToSpeech: TextToSpeech
    var isReady = false
        private set

    init {
        initTextToSpeech(context)
    }

    private fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when (language) {
                    "en" -> Locale.US
                    "ru" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
                    "fi" -> Locale.Builder().setLanguage("fi").setRegion("FI").build()
                    else -> Locale.getDefault()
                }
                val result = textToSpeech.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show()
                } else {
                    isReady = true
                }
            } else {
                Toast.makeText(context, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setLanguage(newLanguage: String) {
        language = newLanguage
        val locale = when (language) {
            "en" -> Locale.US
            "ru" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
            "fi" -> Locale.Builder().setLanguage("fi").setRegion("FI").build()
            else -> Locale.getDefault()
        }
        textToSpeech.language = locale
    }

    fun speak(message: String) {
        if (isReady) {
            val utteranceId = "alarm_utterance"
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }

    fun shutdown() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
    }
}
