package org.accessbridge.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.accessbridge.domain.model.AccessibleSummary
import org.accessbridge.domain.model.SummarySource

@Composable
fun AccessBridgeScreen(
    state: AppUiState,
    isServiceEnabled: Boolean,
    onAiConsentChanged: (Boolean) -> Unit,
    onRequestAiSummary: () -> Unit,
    onSummarize: () -> Unit,
    onReadAll: () -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Text(
                        text = "AccessBridge",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "When apps go silent, AccessBridge speaks.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.isAssistMode) {
                AssistMenu(
                    state = state,
                    onSummarize = onSummarize,
                    onReadAll = onReadAll,
                    onRefresh = onRefresh,
                    onClose = onClose,
                )
            } else if (state.summary == null && !state.isProcessing) {
                WelcomeCard(
                    isServiceEnabled = isServiceEnabled,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                )
            }

            if (state.isProcessing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text(state.message ?: "Processing screenshot…")
                }
            } else if (state.message != null) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }

            if (!state.isAssistMode || state.selectedAction == AssistAction.SUMMARY) {
                state.summary?.let { summary -> SummaryCard(summary) }
            }

            if (state.isAssistMode && state.selectedAction == AssistAction.READ_ALL) {
                ReadAllCard(state.assistText.orEmpty())
            }

            // Release builds carry no AI key, so store users never see an unusable option.
            if (state.isAiConfigured) {
                AiConsentCard(
                    consentGranted = state.aiConsentGranted,
                    isConfigured = state.isAiConfigured,
                    hasRecognizedText = state.recognizedText != null,
                    isProcessing = state.isProcessing,
                    onConsentChanged = onAiConsentChanged,
                    onRequestSummary = onRequestAiSummary,
                )
            }

            Text(
                text = "AccessBridge is independent and not affiliated with the apps it reads.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WelcomeCard(
    isServiceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Share a screenshot to begin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = if (isServiceEnabled) {
                    "Assist Mode is enabled. Use Android’s accessibility shortcut or button while another app is open."
                } else {
                    "Assist Mode is disabled. Enable it manually in Android accessibility settings to use the accessibility shortcut or button."
                },
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (!isServiceEnabled) {
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                ) {
                    Text("Open accessibility settings")
                }
            }
            Text(
                text = "In another app, open Android’s Share menu and choose AccessBridge. " +
                    "Visible text will be recognized on this device and organized for TalkBack.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "AccessBridge does not control the source app or access its private data.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AssistMenu(
    state: AppUiState,
    onSummarize: () -> Unit,
    onReadAll: () -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "What do you want to know?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        AssistButton("Summarize current screen", !state.isProcessing && state.summary != null, onSummarize)
        AssistButton("Read all visible text", !state.isProcessing && state.assistText != null, onReadAll)
        AssistButton("Refresh current screen", !state.isProcessing, onRefresh)
        AssistButton("Close AccessBridge", true, onClose)
    }
}

@Composable
private fun AssistButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp),
    ) {
        Text(label)
    }
}

@Composable
private fun ReadAllCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "All visible text",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SummaryCard(summary: AccessibleSummary) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = summary.headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = when (summary.source) {
                    SummarySource.ON_DEVICE -> "Created on-device"
                    SummarySource.FEATHERLESS_AI -> "Optional AI-assisted summary"
                },
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            )
            summary.sections.forEachIndexed { index, section ->
                if (index > 0) HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = section.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = section.content,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiConsentCard(
    consentGranted: Boolean,
    isConfigured: Boolean,
    hasRecognizedText: Boolean,
    isProcessing: Boolean,
    onConsentChanged: (Boolean) -> Unit,
    onRequestSummary: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Optional AI summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "If enabled, recognized text is sent to Featherless AI. The screenshot " +
                    "itself is never sent. This choice lasts only while the app is open.",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .toggleable(
                        value = consentGranted,
                        role = Role.Switch,
                        onValueChange = onConsentChanged,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (consentGranted) "AI text sharing allowed" else "AI text sharing off",
                    fontWeight = FontWeight.SemiBold,
                )
                Switch(
                    checked = consentGranted,
                    onCheckedChange = null,
                )
            }
            if (!isConfigured) {
                Text(
                    text = "Featherless AI is not configured in this build. On-device summaries still work.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (consentGranted && isConfigured && hasRecognizedText) {
                Button(
                    onClick = onRequestSummary,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp),
                ) {
                    Text("Create AI-assisted summary")
                }
            }
        }
    }
}
