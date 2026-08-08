package org.accessbridge.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.accessbridge.BuildConfig
import org.accessbridge.assist.AssistSessionStore
import org.accessbridge.assist.CaptureIssue
import org.accessbridge.assist.ScreenTextCombiner
import org.accessbridge.data.ocr.MlKitTextRecognitionRepository
import org.accessbridge.data.summary.FeatherlessSummaryRepository
import org.accessbridge.data.summary.RuleBasedSummaryRepository
import org.accessbridge.domain.repository.SummaryRepository
import org.accessbridge.domain.usecase.ProcessSharedScreenshotUseCase

class AccessBridgeViewModel(
    private val processSharedScreenshot: ProcessSharedScreenshotUseCase,
    private val textRecognitionRepository: MlKitTextRecognitionRepository,
    private val localSummaryRepository: SummaryRepository,
    private val aiSummaryRepository: SummaryRepository,
    private val isAiConfigured: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AppUiState(isAiConfigured = isAiConfigured),
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun processScreenshot(sharedImageUri: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = "Recognizing text on this device…",
                )
            }

            runCatching { processSharedScreenshot(sharedImageUri) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            recognizedText = result.recognizedText,
                            summary = result.summary,
                            message = "On-device text recognition complete.",
                        )
                    }

                    if (_uiState.value.aiConsentGranted) requestAiSummary()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            message = error.message ?: "This screenshot could not be processed.",
                        )
                    }
                }
        }
    }

    fun processAssistCapture(captureId: String) {
        val capture = AssistSessionStore.take(captureId) ?: run {
            _uiState.update {
                it.copy(
                    isAssistMode = true,
                    captureState = CaptureState.FAILED,
                    message = "The temporary screen capture expired. Refresh the current screen.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAssistMode = true,
                    isProcessing = true,
                    selectedAction = null,
                    message = "Reading the current screen on this device…",
                )
            }

            val ocrText = try {
                capture.screenshot?.let { textRecognitionRepository.recognize(it) }.orEmpty()
            } catch (_: Exception) {
                ""
            } finally {
                capture.screenshot?.recycle()
            }
            val combinedText = ScreenTextCombiner.combine(capture.accessibilityText, ocrText)
            val captureState = when {
                capture.issue == CaptureIssue.PROTECTED_SCREEN -> CaptureState.PROTECTED
                capture.issue == CaptureIssue.FAILED -> CaptureState.FAILED
                capture.issue == CaptureIssue.UNSUPPORTED -> CaptureState.UNSUPPORTED
                combinedText.isEmpty() -> CaptureState.EMPTY
                else -> CaptureState.READY
            }
            val summary = if (combinedText.isEmpty()) {
                null
            } else {
                localSummaryRepository.summarize(combinedText)
            }

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    recognizedText = combinedText.ifEmpty { null },
                    assistText = combinedText.ifEmpty { null },
                    summary = summary,
                    captureState = captureState,
                    message = captureState.message,
                )
            }
        }
    }

    fun showSummary() {
        if (_uiState.value.recognizedText == null) return
        _uiState.update { it.copy(selectedAction = AssistAction.SUMMARY) }
    }

    fun showAllText() {
        if (_uiState.value.assistText == null) return
        _uiState.update { it.copy(selectedAction = AssistAction.READ_ALL) }
    }

    fun setAiConsent(granted: Boolean) {
        _uiState.update {
            it.copy(
                aiConsentGranted = granted,
                message = when {
                    !granted -> "Optional AI summaries are off."
                    !isAiConfigured -> "AI consent saved, but Featherless is not configured in this build."
                    else -> "AI summaries are on. Only recognized text will be sent."
                },
            )
        }
    }

    fun requestAiSummary() {
        val current = _uiState.value
        val text = current.recognizedText ?: return

        if (!current.aiConsentGranted) {
            _uiState.update { it.copy(message = "Turn on AI summaries before sending text.") }
            return
        }

        if (!isAiConfigured) {
            _uiState.update { it.copy(message = "Featherless is not configured in this build.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isProcessing = true, message = "Creating the optional AI summary…")
            }
            runCatching { aiSummaryRepository.summarize(text) }
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            summary = summary,
                            message = "AI-assisted summary ready.",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            message = error.message ?: "The optional AI summary failed.",
                        )
                    }
                }
        }
    }

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val featherlessRepository = FeatherlessSummaryRepository(
                apiKey = BuildConfig.FEATHERLESS_API_KEY,
                model = BuildConfig.FEATHERLESS_MODEL,
            )
            return AccessBridgeViewModel(
                processSharedScreenshot = ProcessSharedScreenshotUseCase(
                    textRecognitionRepository = MlKitTextRecognitionRepository(appContext),
                    localSummaryRepository = RuleBasedSummaryRepository(),
                ),
                textRecognitionRepository = MlKitTextRecognitionRepository(appContext),
                localSummaryRepository = RuleBasedSummaryRepository(),
                aiSummaryRepository = featherlessRepository,
                isAiConfigured = featherlessRepository.isConfigured,
            ) as T
        }
    }
}

private val CaptureState.message: String
    get() = when (this) {
        CaptureState.READY -> "Current screen ready. Choose what you want to know."
        CaptureState.EMPTY -> "No readable text or content descriptions were found on this screen."
        CaptureState.PROTECTED -> "Protected screen: Android blocked the screenshot. Available accessibility text is shown when present."
        CaptureState.FAILED -> "Screen capture failed. Available accessibility text is shown when present."
        CaptureState.UNSUPPORTED -> "Screenshots require Android 11 or newer. Available accessibility text is ready."
        CaptureState.NONE -> ""
    }
