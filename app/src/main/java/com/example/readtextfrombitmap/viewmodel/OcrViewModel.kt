package com.example.readtextfrombitmap.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readtextfrombitmap.Utils
import com.example.readtextfrombitmap.ocr.OcrManager
import com.example.readtextfrombitmap.Utils.uriToBitmap
import com.example.readtextfrombitmap.model.OcrResults
import com.example.readtextfrombitmap.roomdb.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class OcrViewModel(application:Application): AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val ocrManager = OcrManager(context)
    private val dao by lazy { AppDatabase.getDatabase(getApplication()).roomDao() }

    var isLoading by mutableStateOf(true)
        private set
    val results: StateFlow<List<OcrResults>> = dao.getAllResults()
        .onStart { isLoading=true }
        .onEach { isLoading=false }
        .stateIn(viewModelScope,
        SharingStarted.Lazily, emptyList())

    var text by mutableStateOf("")
        private set

    suspend fun processBitmap(uri: Uri){
        withContext(Dispatchers.IO){
            try{
                val bitmap = uriToBitmap(context, uri)
                bitmap?.let{
                    text  = ocrManager.recognizeText(bitmap)
                }
                val file = saveUriPermanently(context,uri)
                file?.let{
                    insertNewOcr(file)
                }?:run{
                    withContext(Dispatchers.Main){
                        Toast.makeText(context,"Img uri can not saved",Toast.LENGTH_LONG).show()
                    }
                }

            }catch (e:Exception){
                println("Err:View Model :"+e.message)
            }
        }
    }
    // This method copy the file/img to a static location. So we can show it in history.
    private fun saveUriPermanently(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val dir = File(context.filesDir, "ocr_images") // kalıcı dizin
            if (!dir.exists()) dir.mkdir()
            val file = File(dir, "ocr_${System.currentTimeMillis()}.jpg")
            inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            file
        } catch (e: Exception) {
            null
        }
    }
    private suspend fun insertNewOcr(file: File){
        val ocr_result = OcrResults(img = file.path, img_ocr_result = text, ocr_time = Utils.getCurrentDateTime())
        dao.insert(ocr_result)
    }
}