package org.accessbridge.assist

import android.graphics.Bitmap
import java.util.UUID

enum class CaptureIssue {
    NONE,
    UNSUPPORTED,
    FAILED,
    PROTECTED_SCREEN,
}

data class AssistCapture(
    val screenshot: Bitmap?,
    val accessibilityText: String,
    val issue: CaptureIssue,
)

object AssistSessionStore {
    private val captures = mutableMapOf<String, AssistCapture>()

    @Synchronized
    fun put(capture: AssistCapture): String = UUID.randomUUID().toString().also { id ->
        clear()
        captures[id] = capture
    }

    @Synchronized
    fun take(id: String): AssistCapture? = captures.remove(id)

    @Synchronized
    fun clear() {
        captures.values.forEach { it.screenshot?.recycle() }
        captures.clear()
    }
}
