package com.example.readtextfrombitmap.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.readtextfrombitmap.LanguagePrefManager
import com.example.readtextfrombitmap.R
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

class OcrManager(private val context: Context) {
    private var tess:TessBaseAPI
    private val lang_pref:LanguagePrefManager by lazy{
        LanguagePrefManager(context)
    }
    private var currentLang: String? = null
    fun getLangugagePref():LanguagePrefManager=lang_pref

    init {
        tess = TessBaseAPI().apply {
            val dataPath = context.filesDir.absolutePath + "/tesseract/"
            val tessDataDir = File(dataPath, "tessdata")
            if (!tessDataDir.exists()) tessDataDir.mkdirs()

            val lang = lang_pref.getLanguage()
            val trainedDataRes = if (lang == "tur") R.raw.tur else R.raw.eng
            copyAssetIfNeeded(context, trainedDataRes, tessDataDir, "$lang.traineddata")
            init(dataPath, lang)
        }
        currentLang = lang_pref.getLanguage()
    }

    fun initTess(){
        val lang = lang_pref.getLanguage()
        if (lang != currentLang) {
            tess.end()
            val dataPath = context.filesDir.absolutePath + "/tesseract/"
            val tessDataDir = File(dataPath, "tessdata")
            if (!tessDataDir.exists()) tessDataDir.mkdirs()

            val trainedDataRes = if (lang == "tur") R.raw.tur else R.raw.eng
            copyAssetIfNeeded(context, trainedDataRes, tessDataDir, "$lang.traineddata")
            tess.init(dataPath, lang)
            currentLang = lang
        }
    }
    fun recognizeText(bitmap: Bitmap): String {
        return try {
            tess.setImage(bitmap)
            tess.utF8Text
        } finally {
            tess.clear()
        }
    }
    private fun copyAssetIfNeeded(context: Context, resId: Int, outDir: File,outFileName:String) {
        val outFile = File(outDir, outFileName)
        if (!outFile.exists()) {
            outDir.mkdirs()
            try {
                context.resources.openRawResource(resId).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Err: tur file could not be copied :${e.message}")
            }
        }
    }
    fun release() {
        tess.end() // free native source
    }
}