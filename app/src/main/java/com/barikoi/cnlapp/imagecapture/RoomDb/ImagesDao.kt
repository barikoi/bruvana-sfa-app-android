package com.barikoi.cnlapp.imagecapture.RoomDb;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ImagesDao {
    @Query("SELECT * FROM Images WHERE Position =:pos AND FileType =:type")
    fun getImageDBPos(pos: Int?, type: String): List<Images>?

    @Query("SELECT * FROM Images WHERE FileType =:type")
    fun getAllImageDB(type: String): List<Images?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg images: Images)

    @Query("UPDATE Images SET  Position =:position WHERE FilePath=:file_path AND FileType =:type")
    fun updatePosition(file_path: String?, position: Int?, type: String)

    @Query("DELETE FROM Images WHERE Position = :pos AND FileType =:type")
    fun deleteImage(pos: Int?, type: String)

    @Query("DELETE FROM Images")
    fun deleteAllImages()
}