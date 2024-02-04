package com.barikoi.cnlapp.utils.extension

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatDate(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return df.format(this)
}

/**
 * @param {@link Date()}
 * @return Sunday, February 04, 2024
 */
fun Date.formatDateToFullName(): String {
    val df = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
    return df.format(this)
}