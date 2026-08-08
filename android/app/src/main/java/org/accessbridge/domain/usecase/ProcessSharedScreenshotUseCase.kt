package org.accessbridge.domain.usecase

import org.accessbridge.domain.model.ProcessedScreenshot
import org.accessbridge.domain.repository.SummaryRepository
import org.accessbridge.domain.repository.TextRecognitionRepository

class ProcessSharedScreenshotUseCase(
    private val textRecognitionRepository: TextRecognitionRepository,
    private val localSummaryRepository: SummaryRepository,
) {
    suspend operator fun invoke(sharedImageUri: String): ProcessedScreenshot {
        val recognizedText = textRecognitionRepository.recognize(sharedImageUri).trim()
        require(recognizedText.isNotEmpty()) {
            "No readable text was found in this screenshot."
        }

        return ProcessedScreenshot(
            recognizedText = recognizedText,
            summary = localSummaryRepository.summarize(recognizedText),
        )
    }
}
