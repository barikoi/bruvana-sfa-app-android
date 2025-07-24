@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.barikoi.cnlapp.utils.extension

import com.barikoi.cnlapp.utils.AppLogger
import org.threeten.bp.LocalDateTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * @return Date format 2024-02-08
 */
fun Date.formatDateWithLocale(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return df.format(this)
}

fun String.formatDateWithLocale(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // adjust if needed
    val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

    val date = inputFormat.parse(this) ?: return this // fallback to original if parse fails
    return outputFormat.format(date)
}

fun String.formatDateWithDDMM(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // adjust if needed
    val outputFormat = SimpleDateFormat("d MMMM", Locale.getDefault())

    val date = inputFormat.parse(this) ?: return this // fallback to original if parse fails
    return outputFormat.format(date)
}

fun String.formatDateWithDDMMYYYY(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // adjust if needed
    val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

    val date = inputFormat.parse(this) ?: return this // fallback to original if parse fails
    return outputFormat.format(date)
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
    return DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a").format(inst)
}

fun String.formatHumanReadableTime(): String {
    val inst = OffsetDateTime.ofInstant(
        Instant.parse(this),
        ZoneId.systemDefault()
    )
    return DateTimeFormatter.ofPattern("hh:mm a").format(inst)
}


fun compareDateTimes(date1: String, date2: String): String {
    val dateTime1 = org.threeten.bp.OffsetDateTime.parse(date1)
    val dateTime2 = org.threeten.bp.OffsetDateTime.parse(date2)

    return when {
        dateTime1.isBefore(dateTime2) -> "Date 1 is before Date 2"
        dateTime1.isAfter(dateTime2) -> "Date 1 is after Date 2"
        else -> "Date 1 is equal to Date 2"
    }
}

fun String.formateDate(): String {
    val dfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    val dfOutput = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)

    return dfOutput.format(dfInput.parse(this))
}

fun String.formateDateNewFormat(newFormat: String): String {
    val dfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    val dfOutput = SimpleDateFormat(newFormat, Locale.ENGLISH)

    return dfOutput.format(dfInput.parse(this))
}


fun getDifferenceInMinutes(date1: String): Long {
    AppLogger.log("Date1:: $date1")
    val currentDateTime = LocalDateTime.now()
        .format(org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

    AppLogger.log("CurrentDateTime:: $currentDateTime")

    val inputFormatter =
        org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val utcDateTime = org.threeten.bp.ZonedDateTime.parse(
        date1,
        org.threeten.bp.format.DateTimeFormatter.ISO_DATE_TIME
    )

    val localDateTime = utcDateTime.withZoneSameInstant(org.threeten.bp.ZoneId.systemDefault())

    val outputFormatter =
        org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    val dateTime1: Date = format.parse(localDateTime.format(outputFormatter)) ?: return 0
    val dateTime2: Date = format.parse(currentDateTime) ?: return 0

    val differenceInMillis = dateTime2.time - dateTime1.time
    val aa = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis)
    AppLogger.log("Time DIFF:: Current:: $dateTime2 LastUpdate:: $dateTime1 DIFF:: $aa")
    return aa
}

fun String.isoToReadableDate(): String {
    val inputFormatter =
        org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    val dateTime = LocalDateTime.parse(this, inputFormatter)
    val outputFormatter =
        org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    AppLogger.log(":::: ${dateTime.format(outputFormatter)}")
    return dateTime.format(outputFormatter)
}

fun Calendar.formattedDateTime(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    return dateFormat.format(this.time)
}

fun String.getStartDateTime(): String {
    return "$this 00:00:00"
}

fun String.getEndDateTime(): String {
    return "$this 23:59:59"
}