package com.example.readtextfrombitmap.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.readtextfrombitmap.model.OcrResults
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM OcrResultsTab ORDER BY OcrDate desc")
    fun getAllResults(): Flow<List<OcrResults>>

    @Insert
    suspend fun insert(ocrResults: OcrResults)

    @Delete
    suspend fun deleteResult(ocrResults: OcrResults)

    @Query("DELETE FROM OcrResultsTab")
    suspend fun deleteAllResults()
}