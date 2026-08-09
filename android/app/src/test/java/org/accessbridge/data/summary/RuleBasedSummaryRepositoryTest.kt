package org.accessbridge.data.summary

import kotlinx.coroutines.runBlocking
import org.accessbridge.domain.model.SummarySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedSummaryRepositoryTest {
    @Test
    fun `summary keeps recognized content and marks it as on-device`() = runBlocking {
        val summary = RuleBasedSummaryRepository().summarize(
            "Driver approaching\n3 minutes\nPickup Gate B",
        )

        assertEquals(SummarySource.ON_DEVICE, summary.source)
        assertTrue(summary.sections.first().content.contains("Pickup Gate B"))
    }

    @Test
    fun `navigation summary prioritizes route details and separates actions`() = runBlocking {
        val summary = RuleBasedSummaryRepository().summarize(
            "Directions\nDestination Central Station\nTurn left on King Street\n" +
                "2.4 km\n8 minutes\nStart\nCancel",
        )

        assertEquals("Navigation screen information", summary.headline)
        assertTrue(summary.sections.first().content.contains("Turn left on King Street"))
        assertTrue(summary.sections.first().content.contains("8 minutes"))
        assertTrue(summary.sections.last().content.contains("Start"))
        assertTrue(summary.sections.last().content.contains("Cancel"))
    }

    @Test
    fun `local summary highlights warnings without inventing context`() = runBlocking {
        val summary = RuleBasedSummaryRepository().summarize(
            "Payment\nError processing payment\nRetry\nBack",
        )

        assertTrue(summary.sections.first().content.contains("Error processing payment"))
        assertTrue(summary.sections.last().content.contains("Retry"))
        assertTrue(summary.sections.none { it.content.contains("bank", ignoreCase = true) })
    }
}
