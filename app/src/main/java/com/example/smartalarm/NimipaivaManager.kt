package com.example.smartalarm

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.charset.StandardCharsets

class NameDayManager(private val context: Context) {

    fun getTodaysNames(): List<String> {
        val calendar = java.util.Calendar.getInstance()
        val month = (calendar.get(java.util.Calendar.MONTH) + 1).toString() // 1-12
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH).toString()

        return try {
            val nameDayData = loadNameDayData()
            nameDayData[month]?.get(day) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getNameDayMessage(names: List<String>, language: String, context: Context): String {
        if (names.isEmpty()) return ""

        val namesString = names.joinToString(", ")

        val localeString = LocaleManager.getStringResource(context, R.string.tts_name_day, language)
        return String.format(java.util.Locale.getDefault(), localeString, namesString)
    }

    private fun loadNameDayData(): Map<String, Map<String, List<String>>> {
        val jsonString = loadJSONFromAssets("name_days.json")
        val type = object : TypeToken<Map<String, Map<String, List<String>>>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    private fun loadJSONFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                String(buffer, StandardCharsets.UTF_8)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            "{}"
        }
    }
}
