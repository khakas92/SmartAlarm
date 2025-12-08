package com.example.smartalarm

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast

class AlarmTTS(context: Context) {
    private lateinit var textToSpeech: TextToSpeech
    var isReady = false
        private set

    init {
        initTextToSpeech(context)
    }

    private fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
            } else {
                Toast.makeText(context, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
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
