package org.accessbridge.assist

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTextCombinerTest {
    @Test
    fun `combines accessibility and OCR text while removing duplicate lines`() {
        val combined = ScreenTextCombiner.combine(
            accessibilityText = "Pickup point\nThree minutes",
            ocrText = "Three minutes\nDriver ABC",
        )

        assertEquals("Pickup point\nThree minutes\nDriver ABC", combined)
    }

    @Test
    fun `ignores blank lines and trims exposed text`() {
        val combined = ScreenTextCombiner.combine(
            accessibilityText = "  Continue  \n\n",
            ocrText = "   \nCancel ",
        )

        assertEquals("Continue\nCancel", combined)
    }

    @Test
    fun `returns empty text when neither source exposes content`() {
        assertEquals("", ScreenTextCombiner.combine("\n", "  "))
    }
}
