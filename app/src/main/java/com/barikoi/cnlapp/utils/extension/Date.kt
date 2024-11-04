package com.barikoi.cnlapp.utils.extension

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * @return Date format 2024-02-08
 */
fun Date.formatDateWithLocale(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return df.format(this)
}

fun Date.formatDate(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return df.format(this)
}

/**
 * @return Date format February 08, 2024
 */
fun Date.formatFullMonthDateYear(): String {
    val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return simpleFormat.format(this)
}


/**
 * @param {@link Date()}
 * @return formatted date: Sunday, February 04, 2024
 */
fun Date.formatDateToFullName(): String {
    val df = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    return df.format(this)
}

fun String.formatHumanReadableDate(): String {
    val inst = OffsetDateTime.ofInstant(
        Instant.parse(this),
        ZoneId.systemDefault()
    )
    return  DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a").format(inst)
}

fun String.formatHumanReadableTime(): String {
    val inst = OffsetDateTime.ofInstant(
        Instant.parse(this),
        ZoneId.systemDefault()
    )
    return  DateTimeFormatter.ofPattern("hh:mm a").format(inst)
}