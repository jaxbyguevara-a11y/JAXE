package com.aistudio.lookia

import com.aistudio.lookia.ui.format.formatCop
import com.aistudio.lookia.ui.format.formatCopWithCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the Colombian peso formatting introduced for AUDITORIA.md M-03.
 * The screens previously rendered `$420000 COP` with no grouping at all.
 */
class CurrencyFormatTest {

    @Test
    fun `groups thousands instead of printing a bare number`() {
        val formatted = formatCop(420_000.0)

        assertTrue(
            "Expected a grouping separator in '$formatted'",
            formatted.any { it == '.' || it == ',' || it == ' ' },
        )
        assertTrue(formatted.startsWith("$"))
        // Whatever the separator glyph, the digits must be intact.
        assertEquals("420000", formatted.filter { it.isDigit() })
    }

    @Test
    fun `drops centavos`() {
        assertEquals("12500", formatCop(12_499.99).filter { it.isDigit() })
    }

    @Test
    fun `zero renders as zero, not as an empty string`() {
        assertEquals("0", formatCop(0.0).filter { it.isDigit() })
    }

    @Test
    fun `non-finite input degrades to zero rather than printing NaN`() {
        assertEquals("$0", formatCop(Double.NaN))
        assertEquals("$0", formatCop(Double.POSITIVE_INFINITY))
    }

    @Test
    fun `code variant appends COP`() {
        assertTrue(formatCopWithCode(1_000.0).endsWith(" COP"))
    }
}
