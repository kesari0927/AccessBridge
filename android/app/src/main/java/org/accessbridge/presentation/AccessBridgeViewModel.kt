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
import org.accessbridge.data.ocr.MlKitTextRecognitionRepository
import org.accessbridge.data.summary.FeatherlessSummaryRepository
import org.accessbridge.data.summary.RuleBasedSummaryRepository
import org.accessbridge.domain.repository.SummaryRepository
import org.accessbridge.domain.usecase.ProcessSharedScreenshotUseCase

class AccessBridgeViewModel(
    private val processSharedScreenshot: ProcessSharedScreenshotUseCase,
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
                aiSummaryRepository = featherlessRepository,
                isAiConfigured = featherlessRepository.isConfigured,
            ) as T
        }
    }
}
