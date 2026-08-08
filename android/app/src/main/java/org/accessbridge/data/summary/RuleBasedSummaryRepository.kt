package org.accessbridge.data.summary

import org.accessbridge.domain.model.AccessibleSummary
import org.accessbridge.domain.model.SummarySection
import org.accessbridge.domain.model.SummarySource
import org.accessbridge.domain.repository.SummaryRepository

class RuleBasedSummaryRepository : SummaryRepository {
    override suspend fun summarize(recognizedText: String): AccessibleSummary {
        val usefulLines = recognizedText.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .take(MAX_LINES)
            .toList()

        val sections = usefulLines.chunked(LINES_PER_SECTION).mapIndexed { index, lines ->
            SummarySection(
                label = if (index == 0) "Screen information" else "More details",
                content = lines.joinToString(separator = ". ")
                    .replace(Regex("[.]{2,}"), ".")
                    .let { if (it.endsWith('.')) it else "$it." },
            )
        }

        return AccessibleSummary(
            headline = "Text recognized on this screen",
            sections = sections,
            source = SummarySource.ON_DEVICE,
        )
    }

    private companion object {
        const val MAX_LINES = 18
        const val LINES_PER_SECTION = 9
    }
}
