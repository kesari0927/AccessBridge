package org.accessbridge.assist

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.accessbridge.domain.model.AccessibleSummary

class AssistOverlayPanel(
    private val service: AccessibilityService,
    private val callbacks: Callbacks,
    private val isAiConfigured: Boolean,
) {
    interface Callbacks {
        fun onSummarize(useAi: Boolean)
        fun onReadAll()
        fun onRefresh()
        fun onClose()
    }

    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var rootView: View? = null
    private lateinit var statusView: TextView
    private lateinit var resultHeading: TextView
    private lateinit var resultView: TextView
    private lateinit var summarizeButton: Button
    private lateinit var readAllButton: Button
    private lateinit var refreshButton: Button
    private lateinit var aiCheckBox: CheckBox

    fun showLoading(message: String) {
        ensureShown()
        statusView.text = message
        statusView.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        resultHeading.visibility = View.GONE
        resultView.visibility = View.GONE
        setContentActionsEnabled(false)
    }

    fun showReady(message: String, hasText: Boolean) {
        ensureShown()
        statusView.text = message
        summarizeButton.isEnabled = hasText
        readAllButton.isEnabled = hasText
        refreshButton.isEnabled = true
    }

    fun showSummary(summary: AccessibleSummary, message: String) {
        ensureShown()
        statusView.text = message
        resultHeading.text = summary.headline
        resultView.text = summary.sections.joinToString("\n\n") { section ->
            "${section.label}. ${section.content}"
        }
        showResult()
    }

    fun showAllText(text: String) {
        ensureShown()
        statusView.setText(org.accessbridge.R.string.visible_text_ready)
        resultHeading.setText(org.accessbridge.R.string.all_visible_text)
        resultView.text = text
        showResult()
    }

    fun dismiss() {
        rootView?.let { view -> runCatching { windowManager.removeView(view) } }
        rootView = null
    }

    private fun showResult() {
        resultHeading.visibility = View.VISIBLE
        resultView.visibility = View.VISIBLE
        setContentActionsEnabled(true)
    }

    private fun setContentActionsEnabled(enabled: Boolean) {
        summarizeButton.isEnabled = enabled
        readAllButton.isEnabled = enabled
        refreshButton.isEnabled = enabled
    }

    private fun ensureShown() {
        if (rootView != null) return

        val density = service.resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val content = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.rgb(250, 250, 250))
                cornerRadius = dp(20).toFloat()
                setStroke(dp(2), Color.rgb(35, 75, 95))
            }
            elevation = dp(12).toFloat()
        }

        val heading = textView("AccessBridge Assist Mode", 22f).apply {
            setTextColor(Color.rgb(20, 45, 60))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isAccessibilityHeading = true
        }
        content.addView(heading, matchWidth())

        statusView = textView("Preparing current screen…", 16f).apply {
            setPadding(0, dp(8), 0, dp(8))
            setTextColor(Color.DKGRAY)
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        content.addView(statusView, matchWidth())

        aiCheckBox = CheckBox(service).apply {
            text = "Use optional Featherless AI for summaries. Only extracted text is sent."
            minHeight = dp(48)
            setTextColor(Color.BLACK)
        }
        // Without a configured key the box stays unchecked and hidden, so summaries stay on-device.
        if (isAiConfigured) content.addView(aiCheckBox, matchWidth())

        summarizeButton = actionButton("Summarize current screen") {
            callbacks.onSummarize(aiCheckBox.isChecked)
        }
        readAllButton = actionButton("Read all visible text", callbacks::onReadAll)
        refreshButton = actionButton("Refresh current screen", callbacks::onRefresh)
        val closeButton = actionButton("Close AccessBridge", callbacks::onClose)
        listOf(summarizeButton, readAllButton, refreshButton, closeButton).forEach { button ->
            content.addView(button, matchWidth(topMargin = dp(6)))
        }

        resultHeading = textView("", 19f).apply {
            visibility = View.GONE
            setPadding(0, dp(14), 0, dp(4))
            setTextColor(Color.rgb(20, 45, 60))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isAccessibilityHeading = true
        }
        resultView = textView("", 17f).apply {
            visibility = View.GONE
            setTextColor(Color.BLACK)
        }
        content.addView(resultHeading, matchWidth())
        content.addView(resultView, matchWidth())

        val scrollView = ScrollView(service).apply {
            isFillViewport = false
            addView(content)
        }
        rootView = scrollView

        windowManager.addView(
            scrollView,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.BOTTOM
                title = "AccessBridge Assist Mode"
            },
        )
    }

    private fun textView(value: String, size: Float): TextView = TextView(service).apply {
        text = value
        textSize = size
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(service).apply {
        text = label
        contentDescription = label
        minHeight = (48 * service.resources.displayMetrics.density).toInt()
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun matchWidth(topMargin: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply {
        this.topMargin = topMargin
    }
}
