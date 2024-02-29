package com.barikoi.cnlapp.utils.extension

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

/**
 * @return Date format 2024-02-08
 */
fun Date.formatDate(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return df.format(this)
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SimpleDateFormat")
fun String.convertDate(): String? {
    val instant: Instant = Instant.parse(this)
    val myDate = Date.from(instant)
    val formatter = SimpleDateFormat("dd-MM-yy HH:mm:ss")
    return formatter.format(myDate)
}


/**
 * @return Date format February 08, 2024
 */
fun Date.formatFullMonthDateYear(): String {
    val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
    return simpleFormat.format(this)
}


/**
 * @param {@link Date()}
 * @return formatted date: Sunday, February 04, 2024
 */
fun Date.formatDateToFullName(): String {
    val df = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
    return df.format(this)
}