package com.barikoi.cnlapp.utils.extension

import androidx.compose.ui.text.intl.Locale


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