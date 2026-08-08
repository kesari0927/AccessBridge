package org.accessbridge.assist

import android.accessibilityservice.AccessibilityService.ScreenshotResult
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build

object BitmapFactoryCompat {
    @TargetApi(Build.VERSION_CODES.R)
    fun fromScreenshot(result: ScreenshotResult): Bitmap {
        val buffer = result.hardwareBuffer
        try {
            val hardwareBitmap = Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                ?: error("Android did not provide a readable screenshot.")
            return hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false).also {
                hardwareBitmap.recycle()
            }
        } finally {
            buffer.close()
        }
    }
}
