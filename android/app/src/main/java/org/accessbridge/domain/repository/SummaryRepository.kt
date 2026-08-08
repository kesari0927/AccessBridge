package org.accessbridge.domain.repository

import org.accessbridge.domain.model.AccessibleSummary

fun interface SummaryRepository {
    suspend fun summarize(recognizedText: String): AccessibleSummary
}
