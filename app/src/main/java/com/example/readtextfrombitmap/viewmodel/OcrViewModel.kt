package com.example.readtextfrombitmap.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readtextfrombitmap.R
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

    private val _isLoadingProcess = MutableStateFlow(false)
    val isLoadingProcess: StateFlow<Boolean> = _isLoadingProcess

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    val results: StateFlow<List<OcrResults>> = dao.getAllResults()
        .onStart { isLoading=true }
        .onEach { isLoading=false }
        .stateIn(viewModelScope,
        SharingStarted.Lazily, emptyList())

    var text by mutableStateOf("")
        private set

    private suspend fun processBitmap() {
        imageUri?.let{
            _isLoadingProcess.value = true
            try{
                val bitmap = uriToBitmap(context, it)
                bitmap?.let{
                    withContext(Dispatchers.IO){
                        text  = ocrManager.recognizeText(bitmap)
                        ocrManager.release()
                    }
                }
                // last 10 ocr are recorded
                if(results.value.size<10){
                    val file = saveUri(context,it)
                    file?.let{
                        insertNewOcr(file)
                    }
                }
                _isLoadingProcess.value = false
            }catch (e:Exception){
                println("Err:View Model Processing :"+e.message)
                _isLoadingProcess.value = false
            }
        }
    }
    // This method copy the file/img to a static location. So we can show it in history.
    private fun saveUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val dir = File(context.filesDir, "ocr_images") // kalıcı dizin
            if (!dir.exists()) dir.mkdir()
            val file = File(dir, "ocr_${System.currentTimeMillis()}.jpg")
            inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            file
        } catch (e: Exception) {
            println("Err:View Model Save Uri :"+e.message)
            null
        }
    }
    private fun deleteOcrImage(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) file.delete() else false
    }
    private suspend fun insertNewOcr(file: File){
        val ocr_result = OcrResults(img = file.path, img_ocr_result = text, ocr_time = Utils.getCurrentDateTime())
        dao.insert(ocr_result)
    }
    fun deleteOcr(ocr:OcrResults){
        viewModelScope.launch(Dispatchers.IO) {
            ocr.img?.let{deleteOcrImage(it)}
            dao.deleteResult(ocr)
        }
    }
    fun upateImageUri(uri: Uri?) {
        viewModelScope.launch {
            imageUri = uri
            processBitmap()
        }
    }
    fun setOcrTraniedData(selected:String){
        when(selected){
            context.getString(R.string.turkish)->{
                ocrManager.getLangugagePref().setLanguage("tur")
                ocrManager.initTess()
            }
            context.getString(R.string.english)->{
                ocrManager.getLangugagePref().setLanguage("eng")
                ocrManager.initTess()
            }
        }
    }
    fun getOcrTraniedData():String{
        return ocrManager.getLangugagePref().getLanguage()
    }
}