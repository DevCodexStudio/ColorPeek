package online.devcodex.colorpeek

import java.awt.Color

/** Parses and formats the exact hexadecimal forms supported by ColorPeek. */
object ColorParser {
    private val numericPattern = Regex("^(0[xX])([0-9a-fA-F](?:_?[0-9a-fA-F]){7})([uU][lL]?|[lL])?$")

    fun parseNumber(text: String): Color? {
        val match = numericPattern.matchEntire(text) ?: return null
        return parse(match.groupValues[1] + match.groupValues[2].replace("_", ""))
    }

    fun formatNumber(color: Color, original: String): String? {
        val match = numericPattern.matchEntire(original) ?: return null
        val source = match.groupValues[2]
        val formatted = format(color, match.groupValues[1] + source.replace("_", "")) ?: return null
        val digits = formatted.substring(2).iterator()
        return match.groupValues[1] + source.map { if (it == '_') '_' else digits.nextChar() }.joinToString("") + match.groupValues[3]
    }

    /** Kotlin infers unsuffixed positive hex literals as Int or Long by magnitude. */
    fun formatKotlinNumber(color: Color, original: String, expectedLong: Boolean = false): String? {
        val formatted = formatNumber(color, original) ?: return null
        val match = numericPattern.matchEntire(original) ?: return null
        if (match.groupValues[3].isNotEmpty()) return formatted
        val wasLong = expectedLong || match.groupValues[2].replace("_", "").toLong(16) > Int.MAX_VALUE
        val nowLong = color.alpha >= 128
        return when {
            wasLong && !nowLong -> formatted + "L"
            !wasLong && nowLong -> "$formatted.toInt()"
            else -> formatted
        }
    }
    private val pattern = Regex("^(#|0[xX])([0-9a-fA-F]{3}|[0-9a-fA-F]{4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")

    fun parse(text: String): Color? {
        val match = pattern.matchEntire(text) ?: return null
        val digits = match.groupValues[2]
        val expanded = if (digits.length <= 4) digits.flatMap { listOf(it, it) }.joinToString("") else digits
        return try {
            when (expanded.length) {
                6 -> Color(expanded.substring(0, 2).toInt(16), expanded.substring(2, 4).toInt(16), expanded.substring(4, 6).toInt(16))
                8 -> Color(expanded.substring(2, 4).toInt(16), expanded.substring(4, 6).toInt(16), expanded.substring(6, 8).toInt(16), expanded.substring(0, 2).toInt(16))
                else -> null
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    /**
     * Keeps prefix spelling, compact/full form, and letter case of [original].
     * An RGB value is promoted to ARGB when the chooser supplies transparency,
     * because otherwise the selected alpha channel could not be represented.
     */
    fun format(color: Color, original: String): String? {
        val match = pattern.matchEntire(original) ?: return null
        val prefix = match.groupValues[1]
        val oldDigits = match.groupValues[2]
        val hadAlpha = oldDigits.length == 4 || oldDigits.length == 8
        val hasAlpha = hadAlpha || color.alpha != 255
        val full = buildString {
            if (hasAlpha) append(hexByte(color.alpha))
            append(hexByte(color.red))
            append(hexByte(color.green))
            append(hexByte(color.blue))
        }
        val compact = oldDigits.length <= 4
        val digits = if (compact) full.chunked(2).joinToString("") { it.substring(0, 1) } else full
        val lowerCase = oldDigits.any { it in 'a'..'f' } && oldDigits.none { it in 'A'..'F' }
        return prefix + if (lowerCase) digits.lowercase() else digits.uppercase()
    }

    private fun hexByte(value: Int): String = value.toString(16).padStart(2, '0').uppercase()
}
