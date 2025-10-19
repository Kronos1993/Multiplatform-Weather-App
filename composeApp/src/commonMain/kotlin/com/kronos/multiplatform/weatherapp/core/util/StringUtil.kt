package com.kronos.multiplatform.weatherapp.core.util

import kotlin.text.count
import kotlin.text.filter
import kotlin.text.isDigit

fun String.format(vararg args: Any): String {
    var formattedString = this
    var nonIndexedCounter = 0

    args.forEachIndexed { index, arg ->
        // Reemplazo para flags indexados como %1$s, %2$s, etc.
        formattedString = formattedString.replace("%${index + 1}\$s", arg.toString())
    }

    // Reemplazo para placeholders simples (%s) en orden
    while (formattedString.contains("%s")) {
        if (nonIndexedCounter < args.size) {
            formattedString = formattedString.replaceFirst("%s", args[nonIndexedCounter].toString())
            nonIndexedCounter++
        } else {
            throw IllegalArgumentException("Insufficient arguments provided for placeholders.")
        }
    }

    while (formattedString.contains("%d")) {
        if (nonIndexedCounter < args.size) {
            formattedString = formattedString.replaceFirst("%d", args[nonIndexedCounter].toString())
            nonIndexedCounter++
        } else {
            throw IllegalArgumentException("Insufficient arguments provided for placeholders.")
        }
    }

    return formattedString
}


fun String.toNumber(): Number? {
    return try {
        if (isEmpty()) {
            throw NumberFormatException("Empty string")
        } else {
            val cleanedString = replace(Regex("[^0-9.]"), "") // Remove non-numeric characters except dot
            if (cleanedString.countOccurrencesOf(".") > 1) {
                throw NumberFormatException("Multiple decimal points")
            }
            when {
                cleanedString.contains(".") -> cleanedString.toDoubleOrNull() // If contains dot, parse as Double
                else -> cleanedString.toIntOrNull() // Otherwise, parse as Int
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun String.isNumber(): String {
    if (this.isEmpty()) return ""
    val filteredValue = this.filter { it.isDigit() || it == '.' }
    val decimalCount = filteredValue.count { it == '.' }

    return if (decimalCount <= 1) {
        // Limit to 2 decimal places
        if (decimalCount == 1) {
            val decimalIndex = filteredValue.indexOf('.')
            if (decimalIndex != -1 && filteredValue.length > decimalIndex + 3) {
                filteredValue.substring(0, decimalIndex + 3)
            } else {
                filteredValue
            }
        } else {
            filteredValue
        }
    } else {
        // If more than one decimal point, remove the extra ones
        val firstDecimalIndex = filteredValue.indexOf('.')
        if (firstDecimalIndex != -1) {
            // Limit to 2 decimal places after removing extra decimal points
            val valueWithOneDecimal = filteredValue.substring(0, firstDecimalIndex + 1) +
                    filteredValue.substring(firstDecimalIndex + 1).replace(".", "")
            val decimalIndex = valueWithOneDecimal.indexOf('.')
            if (decimalIndex != -1 && valueWithOneDecimal.length > decimalIndex + 3) {
                valueWithOneDecimal.substring(0, decimalIndex + 3)
            } else {
                valueWithOneDecimal
            }
        } else {
            filteredValue
        }
    }
}

fun String.countOccurrencesOf(substring: String): Int {
    return (length - replace(substring, "").length) / substring.length
}

fun String.formatPhoneNumber(): String {
    // Filtrar solo los dígitos del string
    val digits = replace(Regex("[^\\d]"), "")

    // Construir el número formateado dinámicamente
    val formatted = when {
        digits.length <= 3 -> digits // Primer bloque
        digits.length <= 6 -> "${digits.substring(0, 3)}-${digits.substring(3)}" // Dos bloques
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, digits.length.coerceAtMost(10))}" // Tres bloques
    }

    return formatted
}

fun String.calculateCursorPosition(oldText: String, oldCursorPosition: Int): Int {
    var shift = 0
    var cursorPosition = oldCursorPosition

    // Ajustar por eliminación o adición de caracteres no numéricos
    for (i in oldText.indices) {
        if (i < oldCursorPosition && !oldText[i].isDigit()) {
            shift--
        }
    }

    for (i in this.indices) {
        if (i < oldCursorPosition + shift && !this[i].isDigit()) {
            shift++
        }
    }

    cursorPosition += shift
    return cursorPosition
}
