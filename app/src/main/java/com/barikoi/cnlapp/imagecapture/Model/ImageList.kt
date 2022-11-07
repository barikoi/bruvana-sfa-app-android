package com.barikoi.cnlapp.imagecapture.Model

import android.graphics.Bitmap
import java.io.Serializable

class ImageList(
        var selectedBitmap: Bitmap,
        var selectedPath: String,
        var selectedPos: Int,
        var selected: String,
        var selectedFileName: String
):Serializable