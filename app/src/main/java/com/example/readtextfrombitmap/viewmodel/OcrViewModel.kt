package com.example.readtextfrombitmap.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readtextfrombitmap.ocr.OcrManager
import com.example.readtextfrombitmap.Utils.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OcrViewModel(application:Application): AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val ocrManager = OcrManager(context)

    var text by mutableStateOf("")
        private set

    suspend fun processBitmap(uri: Uri){
        withContext(Dispatchers.IO){
            val bitmap = uriToBitmap(context, uri)
            bitmap?.let{
                text  = ocrManager.recognizeText(bitmap)
            }
        }
    }
}