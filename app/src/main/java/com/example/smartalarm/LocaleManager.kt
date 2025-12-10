package com.example.smartalarm

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleManager {
    private fun localeFromCode(language: String): Locale =
        when (language) {
            "en" -> Locale.US
            "ru" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
            "fi" -> Locale.Builder().setLanguage("fi").setRegion("FI").build()
            else -> Locale.US
        }

    fun wrapContext(base: Context, language: String): Context {
        val locale = localeFromCode(language)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    fun getStringResource(context: Context, stringId: Int, language: String): String {
        val locale = localeFromCode(language)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.resources.getString(stringId)
    }
}
