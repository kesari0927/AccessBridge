package org.accessbridge.presentation

import org.accessbridge.domain.model.AccessibleSummary

data class AppUiState(
    val isProcessing: Boolean = false,
    val recognizedText: String? = null,
    val summary: AccessibleSummary? = null,
    val aiConsentGranted: Boolean = false,
    val isAiConfigured: Boolean = false,
    val message: String? = null,
)
