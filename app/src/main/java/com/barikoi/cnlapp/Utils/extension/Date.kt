package com.barikoi.cnlapp.Utils.extension

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatDate(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return df.format(this)
}