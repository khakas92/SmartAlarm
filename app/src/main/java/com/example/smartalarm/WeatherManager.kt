package com.example.smartalarm

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherManager(private val apiKey: String, private val context: Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(WeatherService::class.java)

    suspend fun getWeather(city: String, language: String): String {
        return try {
            val response = service.getWeather(city, apiKey, lang = language)
            val temp = response.main.temp.toInt()
            val description = response.weather[0].description

            context.getString(R.string.tts_weather, city, temp, description)
        } catch (e: Exception) {
            context.getString(R.string.tts_error_weather)
        }
    }

}
