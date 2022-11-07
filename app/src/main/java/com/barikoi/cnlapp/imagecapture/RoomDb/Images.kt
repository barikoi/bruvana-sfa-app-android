package com.barikoi.cnlapp.imagecapture.RoomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Images(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "Position") var position: Int,
    @ColumnInfo(name = "FilePath") var filePath: String
)

