package org.accessbridge.assist

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityTextExtractor {
    fun extract(root: AccessibilityNodeInfo?): String {
        if (root == null) return ""

        val lines = mutableListOf<String>()
        val pending = ArrayDeque<AccessibilityNodeInfo>()
        pending.add(root)

        while (pending.isNotEmpty()) {
            val node = pending.removeFirst()
            node.text?.toString()?.trim()?.takeIf(String::isNotEmpty)?.let(lines::add)
            node.contentDescription?.toString()?.trim()?.takeIf(String::isNotEmpty)?.let(lines::add)
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(pending::add)
            }
        }

        return lines.distinct().joinToString("\n")
    }
}

object ScreenTextCombiner {
    fun combine(accessibilityText: String, ocrText: String): String =
        (accessibilityText.lineSequence() + ocrText.lineSequence())
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .joinToString("\n")
}
