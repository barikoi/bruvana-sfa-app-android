package com.barikoi.cnlapp.imagecapture.RoomDb;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ImagesDao {
    @Query("SELECT * FROM Images WHERE Position =:pos")
    fun getImageDBPos(pos: Int?): List<Images>?

    /*@Query("SELECT * FROM Images WHERE ShopDbId =:id")
    fun getImageDB(id: Int?): List<Images>?

    @Query("SELECT * FROM Images WHERE ShopId =:id")
    fun getImageDBShopId(id: String?): List<Images>?*/

    @Query("SELECT * FROM Images")
    fun getAllImageDB(): List<Images?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg images: Images?)

    @Query("UPDATE Images SET  Position =:position WHERE FilePath=:file_path")
    fun updatePosition(file_path: String?, position: Int?)

    @Query("DELETE FROM Images WHERE Position = :pos")
    fun deleteImage(pos: Int?)

    /*@Query("DELETE FROM Images WHERE ShopDbId = :id")
    fun deleteImageByShopId(id: Int?)
*/
    @Query("DELETE FROM Images")
    fun deleteAllImages()
}