package org.accessbridge.data.summary

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.accessbridge.domain.model.AccessibleSummary
import org.accessbridge.domain.model.SummarySection
import org.accessbridge.domain.model.SummarySource
import org.accessbridge.domain.repository.SummaryRepository
import org.json.JSONArray
import org.json.JSONObject

class FeatherlessSummaryRepository(
    private val apiKey: String,
    private val model: String,
) : SummaryRepository {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    override suspend fun summarize(recognizedText: String): AccessibleSummary =
        withContext(Dispatchers.IO) {
            check(isConfigured) {
                "Featherless AI is not configured in this build."
            }

            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                doOutput = true
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("HTTP-Referer", "https://github.com/kesari0927/AccessBridge")
                setRequestProperty("X-Title", "AccessBridge")
            }

            try {
                val requestBody = JSONObject()
                    .put("model", model)
                    .put("temperature", 0.2)
                    .put("max_tokens", 300)
                    .put(
                        "messages",
                        JSONArray()
                            .put(
                                JSONObject()
                                    .put("role", "system")
                                    .put(
                                        "content",
                                        ScreenSummaryPrompt.SYSTEM_PROMPT,
                                    ),
                            )
                            .put(
                                JSONObject()
                                    .put("role", "user")
                                    .put("content", ScreenSummaryPrompt.userMessage(recognizedText)),
                            ),
                    )

                connection.outputStream.bufferedWriter().use { writer ->
                    writer.write(requestBody.toString())
                }

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val responseText = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()

                check(responseCode in 200..299) {
                    val apiMessage = runCatching {
                        JSONObject(responseText).optJSONObject("error")?.optString("message")
                    }.getOrNull().orEmpty()
                    if (apiMessage.isBlank()) {
                        "The optional AI summary could not be created (HTTP $responseCode)."
                    } else {
                        "The optional AI summary could not be created: $apiMessage"
                    }
                }

                val content = JSONObject(responseText)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                check(content.isNotEmpty()) {
                    "Featherless returned an empty summary."
                }

                AccessibleSummary(
                    headline = "AI-assisted screen summary",
                    sections = listOf(
                        SummarySection(
                            label = "What is on this screen",
                            content = content,
                        ),
                    ),
                    source = SummarySource.FEATHERLESS_AI,
                )
            } finally {
                connection.disconnect()
            }
        }

    private companion object {
        const val ENDPOINT = "https://api.featherless.ai/v1/chat/completions"
        const val TIMEOUT_MILLIS = 20_000
    }
}
