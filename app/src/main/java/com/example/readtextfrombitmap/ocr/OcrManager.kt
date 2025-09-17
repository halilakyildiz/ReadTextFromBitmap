package com.example.readtextfrombitmap.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.readtextfrombitmap.R
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

class OcrManager(private val context: Context) {
    fun recognizeText(bitmap: Bitmap): String {
        return try{
            val tess = TessBaseAPI()
            val dataPath = context.filesDir.absolutePath + "/tesseract/"
            val tessDataDir = File(dataPath, "tessdata")
            println(dataPath)
            // raw içindeki tur.traineddata dosyasını kopyala
            copyAssetIfNeeded(context, R.raw.tur, tessDataDir, "tur.traineddata")
            tess.init(dataPath, "tur")
            //println("Bitmap boyutu: ${bitmap.width}x${bitmap.height}")
            tess.setImage(bitmap)
            val extractedText = tess.utF8Text
            tess.end()
            extractedText
        }catch (e:Exception){
            println(e.message)
            e.message ?: "Hata !"
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
                println("File copied successfully: ${outFile.absolutePath}")
            } catch (e: Exception) {
                e.printStackTrace()
                println("File could not be copied!")
            }
        } else {
            println("File already exists: ${outFile.absolutePath}")
        }
    }
}