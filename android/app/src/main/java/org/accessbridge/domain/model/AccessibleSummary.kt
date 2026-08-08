package org.accessbridge.domain.model

data class AccessibleSummary(
    val headline: String,
    val sections: List<SummarySection>,
    val source: SummarySource,
)

data class SummarySection(
    val label: String,
    val content: String,
)

enum class SummarySource {
    ON_DEVICE,
    FEATHERLESS_AI,
}
