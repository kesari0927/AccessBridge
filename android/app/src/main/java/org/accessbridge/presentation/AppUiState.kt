package org.accessbridge.presentation

import org.accessbridge.domain.model.AccessibleSummary

data class AppUiState(
    val isProcessing: Boolean = false,
    val recognizedText: String? = null,
    val summary: AccessibleSummary? = null,
    val aiConsentGranted: Boolean = false,
    val isAiConfigured: Boolean = false,
    val message: String? = null,
    val isAssistMode: Boolean = false,
    val assistText: String? = null,
    val captureState: CaptureState = CaptureState.NONE,
    val selectedAction: AssistAction? = null,
)

enum class CaptureState {
    NONE,
    READY,
    FAILED,
    PROTECTED,
    UNSUPPORTED,
    EMPTY,
}

enum class AssistAction {
    SUMMARY,
    READ_ALL,
}
