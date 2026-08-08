package org.accessbridge

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.accessbridge.presentation.AccessBridgeScreen
import org.accessbridge.presentation.AccessBridgeViewModel
import org.accessbridge.presentation.theme.AccessBridgeTheme

class MainActivity : ComponentActivity() {
    private var sharedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedImageUri = intent.sharedImageUri()

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

                AccessBridgeScreen(
                    state = state,
                    onAiConsentChanged = viewModel::setAiConsent,
                    onRequestAiSummary = viewModel::requestAiSummary,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedImageUri = intent.sharedImageUri()
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
