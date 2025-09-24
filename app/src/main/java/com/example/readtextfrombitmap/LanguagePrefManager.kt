package com.example.readtextfrombitmap

import android.content.Context
import java.util.Locale

class LanguagePrefManager(context:Context) {
    private val prefs = context.getSharedPreferences("ocr_prefs", Context.MODE_PRIVATE)

    fun getLanguage(): String {
        return prefs.getString("ocr_lang", getDefaultLang()) ?: getDefaultLang()
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("ocr_lang", lang).apply()
    }

    private fun getDefaultLang(): String {
        val locale = Locale.getDefault().language
        return if (locale == "tr") "tur" else "eng"
    }
}