package org.accessbridge.data.summary

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenSummaryPromptTest {
    @Test
    fun `prompt requests screen explanation and guards against hallucination`() {
        assertTrue(ScreenSummaryPrompt.SYSTEM_PROMPT.contains("instead of merely shortening"))
        assertTrue(ScreenSummaryPrompt.SYSTEM_PROMPT.contains("Never invent"))
        assertTrue(ScreenSummaryPrompt.SYSTEM_PROMPT.contains("maps or navigation"))
        assertTrue(ScreenSummaryPrompt.SYSTEM_PROMPT.contains("TalkBack"))
    }

    @Test
    fun `user message contains extracted text and no image payload`() {
        val message = ScreenSummaryPrompt.userMessage("Destination Central Station")

        assertTrue(message.contains("Destination Central Station"))
        assertTrue(message.contains("extracted text"))
        assertFalse(message.contains("data:image"))
    }
}
