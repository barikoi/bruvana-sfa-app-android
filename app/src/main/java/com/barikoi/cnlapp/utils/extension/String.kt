package com.barikoi.cnlapp.utils.extension

import android.annotation.SuppressLint
import androidx.compose.ui.text.intl.Locale
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date


fun String.englishToBanglaNumber(): String {
    if (Locale.current.language == "en") return this
    val banglaNumerals = mapOf(
        "0" to "০",
        "1" to "১",
        "2" to "২",
        "3" to "৩",
        "4" to "৪",
        "5" to "৫",
        "6" to "৬",
        "7" to "৭",
        "8" to "৮",
        "9" to "৯"
    )

    var banglaNumber = ""
    for (digit in this) {
        banglaNumber += banglaNumerals[digit.toString()] ?: digit // Use original digit if not found
    }
    return banglaNumber
}

fun String?.totalAmountFormatted(): String {
    if (this == null || this == "null" || this == "") {
        return "0.00"
    }
    return BigDecimal(this.toDouble()).setScale(2, RoundingMode.HALF_UP)
        .toString()
}

fun String.format(): String {
    return DecimalFormat("#.##")
        .format(this.toDouble())
        .toString()
}

@SuppressLint("SimpleDateFormat")
fun String.convertDate(): String? {
    val instant: Instant = Instant.parse(this)
    val myDate = Date.from(instant)
    val formatter = SimpleDateFormat("dd MMMM, yyyy HH:mm")
    return formatter.format(myDate)
}
