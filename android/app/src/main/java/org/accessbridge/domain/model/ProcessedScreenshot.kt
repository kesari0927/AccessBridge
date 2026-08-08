package org.accessbridge.domain.model

data class ProcessedScreenshot(
    val recognizedText: String,
    val summary: AccessibleSummary,
)
