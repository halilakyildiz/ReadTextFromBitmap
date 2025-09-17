package com.example.readtextfrombitmap.model

import android.icu.text.DateFormat
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OcrResultsTab")
class OcrResults(
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    @ColumnInfo("ImgUri")
    var img:String?,
    @ColumnInfo("ImgOcrResult")
    var img_ocr_result:String,
    @ColumnInfo("OcrDate")
    var ocr_time:String
)