package com.allubie.nana.utils

import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

/**
 * Parses a localized decimal number string into Double, supporting comma or dot decimals.
 * Returns null when input is blank or cannot be parsed.
 */
fun parseLocalizedDouble(input: String): Double? {
    val s = input.trim()
    if (s.isEmpty()) return null
    return try {
        val nf = NumberFormat.getNumberInstance(Locale.getDefault())
        nf.parse(s)?.toDouble()
    } catch (_: ParseException) {
        // Fallback: normalize common cases (comma as decimal separator)
        s.replace(',', '.').toDoubleOrNull()
    }
}
