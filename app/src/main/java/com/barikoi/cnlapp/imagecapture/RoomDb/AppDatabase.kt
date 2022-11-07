package com.barikoi.cnlapp.imagecapture.RoomDb

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors


@Database(entities = [Images::class], version = 1, exportSchema = false)
abstract class AppDatabase() : RoomDatabase() {
    abstract fun imagesDao(): ImagesDao?

    companion object {
        var INSTANCE: AppDatabase? = null
        private val sLock = Any()
        private val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(
            NUMBER_OF_THREADS
        )
        /*val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE VerifyImage "
                            + "ADD COLUMN Position INTEGER"
                )
            }
        }*/

        fun getInstance(context: Context): AppDatabase? {
            synchronized(sLock) {
                if (INSTANCE == null) {
                    Log.d("Room", "getInstance")
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase::class.java, "ImageCapture.db"
                    )
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
                Log.d("Room", "getInstance: " + INSTANCE)
                return INSTANCE
            }
        }
    }
}