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
}
