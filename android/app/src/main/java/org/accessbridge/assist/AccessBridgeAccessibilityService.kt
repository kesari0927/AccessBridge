package org.accessbridge.assist

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.accessbridge.BuildConfig
import org.accessbridge.data.ocr.MlKitTextRecognitionRepository
import org.accessbridge.data.summary.FeatherlessSummaryRepository
import org.accessbridge.data.summary.RuleBasedSummaryRepository
import org.accessbridge.domain.model.AccessibleSummary

class AccessBridgeAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var overlay: AssistOverlayPanel
    private lateinit var textRecognizer: MlKitTextRecognitionRepository
    private val localSummaryRepository = RuleBasedSummaryRepository()
    private val aiSummaryRepository by lazy {
        FeatherlessSummaryRepository(
            apiKey = BuildConfig.FEATHERLESS_API_KEY,
            model = BuildConfig.FEATHERLESS_MODEL,
        )
    }
    private var extractedText: String = ""
    private var localSummary: AccessibleSummary? = null
    private var captureInProgress = false
    private var operationId = 0
    private val accessibilityButtonCallback = object :
        AccessibilityButtonController.AccessibilityButtonCallback() {
        override fun onClicked(controller: AccessibilityButtonController) {
            captureAndOpen()
        }
    }

    override fun onServiceConnected() {
        textRecognizer = MlKitTextRecognitionRepository(applicationContext)
        overlay = AssistOverlayPanel(
            service = this,
            isAiConfigured = aiSummaryRepository.isConfigured,
            callbacks = object : AssistOverlayPanel.Callbacks {
                override fun onSummarize(useAi: Boolean) = summarize(useAi)
                override fun onReadAll() = overlay.showAllText(extractedText)
                override fun onRefresh() = refresh()
                override fun onClose() = closeOverlay()
            },
        )
        accessibilityButtonController.registerAccessibilityButtonCallback(
            accessibilityButtonCallback,
            mainHandler,
        )
        // Connecting is how Android's configured accessibility shortcut activates a service.
        mainHandler.post(::captureAndOpen)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        accessibilityButtonController.unregisterAccessibilityButtonCallback(
            accessibilityButtonCallback,
        )
        if (::overlay.isInitialized) overlay.dismiss()
        AssistSessionStore.clear()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (::overlay.isInitialized) overlay.dismiss()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CAPTURE) {
            mainHandler.postDelayed(::captureAndOpen, CAPTURE_DELAY_MILLIS)
        }
        return START_NOT_STICKY
    }

    private fun captureAndOpen() {
        if (captureInProgress) return
        captureInProgress = true
        val operation = ++operationId
        if (::overlay.isInitialized) overlay.dismiss()
        val accessibilityText = AccessibilityTextExtractor.extract(rootInActiveWindow)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            processCapture(
                AssistCapture(
                    screenshot = null,
                    accessibilityText = accessibilityText,
                    issue = CaptureIssue.UNSUPPORTED,
                ),
                operation,
            )
            return
        }

        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    val bitmap = runCatching {
                        BitmapFactoryCompat.fromScreenshot(screenshot)
                    }.getOrElse {
                        processCapture(
                            AssistCapture(
                                screenshot = null,
                                accessibilityText = accessibilityText,
                                issue = CaptureIssue.FAILED,
                            ),
                            operation,
                        )
                        return
                    }
                    processCapture(
                        AssistCapture(
                            screenshot = bitmap,
                            accessibilityText = accessibilityText,
                            issue = CaptureIssue.NONE,
                        ),
                        operation,
                    )
                }

                override fun onFailure(errorCode: Int) {
                    processCapture(
                        AssistCapture(
                            screenshot = null,
                            accessibilityText = accessibilityText,
                            issue = if (errorCode == ERROR_TAKE_SCREENSHOT_SECURE_WINDOW) {
                                CaptureIssue.PROTECTED_SCREEN
                            } else {
                                CaptureIssue.FAILED
                            },
                        ),
                        operation,
                    )
                }
            },
        )
    }

    private fun processCapture(capture: AssistCapture, operation: Int) {
        if (operation != operationId) {
            capture.screenshot?.recycle()
            return
        }
        overlay.showLoading("Reading the current screen on this device…")
        serviceScope.launch {
            val ocrText = try {
                capture.screenshot?.let { textRecognizer.recognize(it) }.orEmpty()
            } catch (_: Exception) {
                ""
            } finally {
                capture.screenshot?.recycle()
            }
            if (operation != operationId) return@launch
            extractedText = ScreenTextCombiner.combine(capture.accessibilityText, ocrText)
            localSummary = extractedText.takeIf(String::isNotEmpty)?.let {
                localSummaryRepository.summarize(it)
            }
            captureInProgress = false
            overlay.showReady(capture.message(extractedText.isNotEmpty()), extractedText.isNotEmpty())
        }
    }

    private fun summarize(useAi: Boolean) {
        val fallback = localSummary ?: return
        val operation = operationId
        serviceScope.launch {
            if (operation != operationId) return@launch
            if (!useAi || !aiSummaryRepository.isConfigured) {
                overlay.showSummary(fallback, "On-device summary ready.")
                return@launch
            }

            overlay.showLoading("Creating an optional AI screen explanation…")
            val aiSummary = runCatching { aiSummaryRepository.summarize(extractedText) }
            if (operation != operationId) return@launch
            aiSummary.onSuccess { summary ->
                overlay.showSummary(summary, "AI screen explanation ready.")
            }.onFailure {
                overlay.showSummary(
                    fallback,
                    "Featherless is unavailable. Showing the on-device summary instead.",
                )
            }
        }
    }

    private fun refresh() {
        overlay.dismiss()
        mainHandler.postDelayed(::captureAndOpen, CAPTURE_DELAY_MILLIS)
    }

    private fun closeOverlay() {
        operationId++
        captureInProgress = false
        overlay.dismiss()
        extractedText = ""
        localSummary = null
    }

    companion object {
        const val ACTION_CAPTURE = "org.accessbridge.action.CAPTURE"
        private const val CAPTURE_DELAY_MILLIS = 350L
    }
}

private fun AssistCapture.message(hasText: Boolean): String = when {
    issue == CaptureIssue.PROTECTED_SCREEN ->
        "Protected screen: Android blocked the screenshot. Available accessibility text is ready."
    issue == CaptureIssue.FAILED ->
        "Screen capture failed. Available accessibility text is ready when exposed by the app."
    issue == CaptureIssue.UNSUPPORTED ->
        "Screenshots require Android 11 or newer. Available accessibility text is ready."
    !hasText -> "No readable text or content descriptions were found on this screen."
    else -> "Current screen ready. Choose what you want to know."
}
