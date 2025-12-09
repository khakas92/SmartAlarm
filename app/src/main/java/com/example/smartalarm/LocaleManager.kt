package com.example.smartalarm

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

object LocaleManager {
    fun setLocale(context: Context, language: String) {
        val locale = when (language) {
            "en" -> Locale.US
            "ru" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
            "fi" -> Locale.Builder().setLanguage("fi").setRegion("FI").build()
            else -> Locale.US
        }

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun getStringResource(context: Context, stringId: Int, language: String): String {
        val locale = when (language) {
            "en" -> Locale.US
            "ru" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
            "fi" -> Locale.Builder().setLanguage("fi").setRegion("FI").build()
            else -> Locale.US
        }

        val resources = context.createConfigurationContext(
            Configuration(context.resources.configuration).apply {
                setLocale(locale)
            }
        ).resources

        return resources.getString(stringId)
    }
}
