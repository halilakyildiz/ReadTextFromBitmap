package com.example.readtextfrombitmap.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.readtextfrombitmap.model.OcrResults

@Database(entities = [OcrResults::class], version = 1,exportSchema = false)
abstract class AppDatabase:RoomDatabase(){
    abstract fun roomDao(): RoomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "OcrResultsTab"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}