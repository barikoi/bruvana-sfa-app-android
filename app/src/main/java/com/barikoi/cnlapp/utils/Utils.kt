package com.barikoi.cnlapp.utils

fun getUserType(type: String): String {
    return when (type) {
        "SO" -> "SR"
        "ASM" -> "RSM"
        "TO" -> "TSM"
        else -> "Unknown"
    }
}