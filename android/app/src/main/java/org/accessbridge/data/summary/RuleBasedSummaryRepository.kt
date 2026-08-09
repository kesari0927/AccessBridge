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

        val importantLines = usefulLines.filter(::isImportant)
        val actionLines = usefulLines.filter(::isLikelyAction)
        val otherLines = usefulLines - importantLines.toSet() - actionLines.toSet()
        val sections = buildList {
            val keyInformation = (importantLines + otherLines).distinct().take(LINES_PER_SECTION)
            if (keyInformation.isNotEmpty()) {
                add(SummarySection("Key screen information", keyInformation.asSentence()))
            }
            if (actionLines.isNotEmpty()) {
                add(SummarySection("Visible actions", actionLines.take(MAX_ACTIONS).asSentence()))
            }
            val remaining = otherLines.drop(keyInformation.count { it in otherLines })
            if (remaining.isNotEmpty()) {
                add(SummarySection("More visible text", remaining.asSentence()))
            }
        }

        return AccessibleSummary(
            headline = if (usefulLines.count(::hasNavigationSignal) >= 2) {
                "Navigation screen information"
            } else {
                "Current screen information"
            },
            sections = sections,
            source = SummarySource.ON_DEVICE,
        )
    }

    private fun isImportant(line: String): Boolean =
        hasNavigationSignal(line) || IMPORTANT_PATTERN.containsMatchIn(line)

    private fun hasNavigationSignal(line: String): Boolean =
        NAVIGATION_PATTERN.containsMatchIn(line)

    private fun isLikelyAction(line: String): Boolean =
        line.length <= MAX_ACTION_LENGTH && ACTION_PATTERN.containsMatchIn(line)

    private fun List<String>.asSentence(): String = joinToString(separator = ". ")
        .replace(Regex("[.]{2,}"), ".")
        .let { if (it.endsWith('.')) it else "$it." }

    private companion object {
        const val MAX_LINES = 18
        const val LINES_PER_SECTION = 9
        const val MAX_ACTIONS = 6
        const val MAX_ACTION_LENGTH = 60
        val NAVIGATION_PATTERN = Regex(
            "\\b(destination|directions?|route|arriv(?:e|al)|turn|continue on|" +
                "street|road|avenue|highway|pickup|drop.?off)\\b",
            RegexOption.IGNORE_CASE,
        )
        val IMPORTANT_PATTERN = Regex(
            "\\b(\\d+(?:[.:]\\d+)?\\s?(?:min(?:ute)?s?|hours?|km|mi|m)|" +
                "warning|error|failed|offline|alert)\\b",
            RegexOption.IGNORE_CASE,
        )
        val ACTION_PATTERN = Regex(
            "^(back|cancel|close|confirm|continue|done|next|open|retry|search|" +
                "start|stop|submit|refresh|directions|navigate)(?:\\b|$)",
            RegexOption.IGNORE_CASE,
        )
    }
}
