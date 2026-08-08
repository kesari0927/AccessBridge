package org.accessbridge

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.accessbridge.assist.AccessBridgeAccessibilityService
import org.accessbridge.presentation.AccessBridgeScreen
import org.accessbridge.presentation.AccessBridgeViewModel
import org.accessbridge.presentation.theme.AccessBridgeTheme

class MainActivity : ComponentActivity() {
    private var sharedImageUri by mutableStateOf<Uri?>(null)
    private var assistCaptureId by mutableStateOf<String?>(null)
    private var isServiceEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedImageUri = intent.sharedImageUri()
        assistCaptureId = intent.getStringExtra(EXTRA_ASSIST_CAPTURE_ID)
        isServiceEnabled = isAccessibilityServiceEnabled()

        setContent {
            AccessBridgeTheme {
                val viewModel: AccessBridgeViewModel = viewModel(
                    factory = AccessBridgeViewModel.Factory(applicationContext),
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(sharedImageUri) {
                    sharedImageUri?.let { uri ->
                        viewModel.processScreenshot(uri.toString())
                        sharedImageUri = null
                    }
                }

                LaunchedEffect(assistCaptureId) {
                    assistCaptureId?.let { id ->
                        viewModel.processAssistCapture(id)
                        assistCaptureId = null
                    }
                }

                AccessBridgeScreen(
                    state = state,
                    isServiceEnabled = isServiceEnabled,
                    onAiConsentChanged = viewModel::setAiConsent,
                    onRequestAiSummary = viewModel::requestAiSummary,
                    onSummarize = viewModel::showSummary,
                    onReadAll = viewModel::showAllText,
                    onRefresh = ::refreshAssistMode,
                    onClose = ::finishAndRemoveTask,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedImageUri = intent.sharedImageUri()
        assistCaptureId = intent.getStringExtra(EXTRA_ASSIST_CAPTURE_ID)
    }

    override fun onResume() {
        super.onResume()
        isServiceEnabled = isAccessibilityServiceEnabled()
    }

    private fun refreshAssistMode() {
        startService(
            Intent(this, AccessBridgeAccessibilityService::class.java)
                .setAction(AccessBridgeAccessibilityService.ACTION_CAPTURE),
        )
        finishAndRemoveTask()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, AccessBridgeAccessibilityService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    companion object {
        const val EXTRA_ASSIST_CAPTURE_ID = "org.accessbridge.extra.ASSIST_CAPTURE_ID"
    }
}

private fun Intent.sharedImageUri(): Uri? {
    if (action != Intent.ACTION_SEND || type?.startsWith("image/") != true) return null

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(Intent.EXTRA_STREAM)
    }
}
