package online.devcodex.colorpeek

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.awt.Color

class ColorParserTest {
    @Test
    fun parsesAllSupportedLengthsAndPrefixes() {
        assertEquals(Color(0xff, 0x55, 0x33), ColorParser.parse("#F53"))
        assertEquals(Color(0xff, 0x55, 0x33, 0xaa), ColorParser.parse("#AF53"))
        assertEquals(Color(0xff, 0x57, 0x33), ColorParser.parse("0xff5733"))
        assertEquals(Color(0xff, 0x57, 0x33, 0x80), ColorParser.parse("0x80ff5733"))
    }

    @Test
    fun rejectsPartialAndUnsupportedValues() {
        assertNull(ColorParser.parse("color=#fff"))
        assertNull(ColorParser.parse("#12"))
        assertNull(ColorParser.parse("0x12345"))
        assertNull(ColorParser.parse("#GGG"))
    }

    @Test
    fun preservesPrefixWidthAndCase() {
        val color = Color(0xab, 0xcd, 0xef, 0x78)
        assertEquals("#7ACE", ColorParser.format(color, "#ABC"))
        assertEquals("0x7ace", ColorParser.format(color, "0xabc"))
        assertEquals("#78ABCDEF", ColorParser.format(color, "#12ABCDEF"))
        assertEquals("0X78abcdef", ColorParser.format(color, "0X12abcdef"))
    }

    @Test
    fun promotesRgbToArgbWhenAlphaChanges() {
        assertEquals("#CC9E2525", ColorParser.format(Color(0x9e, 0x25, 0x25, 0xcc), "#9E2525"))
        assertEquals("0xcc9e2525", ColorParser.format(Color(0x9e, 0x25, 0x25, 0xcc), "0x9e2525"))
        assertEquals("#C923", ColorParser.format(Color(0x99, 0x22, 0x33, 0xcc), "#923"))
        assertEquals("#9E2525", ColorParser.format(Color(0x9e, 0x25, 0x25), "#9E2525"))
    }
}
