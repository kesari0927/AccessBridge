package org.accessbridge.assist

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import org.accessbridge.MainActivity

class AccessBridgeAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val accessibilityButtonCallback = object :
        AccessibilityButtonController.AccessibilityButtonCallback() {
        override fun onClicked(controller: AccessibilityButtonController) {
            captureAndOpen()
        }
    }

    override fun onServiceConnected() {
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
        AssistSessionStore.clear()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CAPTURE) {
            mainHandler.postDelayed(::captureAndOpen, CAPTURE_DELAY_MILLIS)
        }
        return START_NOT_STICKY
    }

    private fun captureAndOpen() {
        val accessibilityText = AccessibilityTextExtractor.extract(rootInActiveWindow)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            openMenu(
                AssistCapture(
                    screenshot = null,
                    accessibilityText = accessibilityText,
                    issue = CaptureIssue.UNSUPPORTED,
                ),
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
                        openMenu(
                            AssistCapture(
                                screenshot = null,
                                accessibilityText = accessibilityText,
                                issue = CaptureIssue.FAILED,
                            ),
                        )
                        return
                    }
                    openMenu(
                        AssistCapture(
                            screenshot = bitmap,
                            accessibilityText = accessibilityText,
                            issue = CaptureIssue.NONE,
                        ),
                    )
                }

                override fun onFailure(errorCode: Int) {
                    openMenu(
                        AssistCapture(
                            screenshot = null,
                            accessibilityText = accessibilityText,
                            issue = if (errorCode == ERROR_TAKE_SCREENSHOT_SECURE_WINDOW) {
                                CaptureIssue.PROTECTED_SCREEN
                            } else {
                                CaptureIssue.FAILED
                            },
                        ),
                    )
                }
            },
        )
    }

    private fun openMenu(capture: AssistCapture) {
        val captureId = AssistSessionStore.put(capture)
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ASSIST_CAPTURE_ID, captureId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
    }

    companion object {
        const val ACTION_CAPTURE = "org.accessbridge.action.CAPTURE"
        private const val CAPTURE_DELAY_MILLIS = 350L
    }
}
