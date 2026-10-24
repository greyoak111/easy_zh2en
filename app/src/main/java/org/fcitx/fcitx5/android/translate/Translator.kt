/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 easy_zh2en contributors
 */
package org.fcitx.fcitx5.android.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * One request to an OpenAI-compatible /chat/completions endpoint.
 * Asks for JSON {"translation": ..., "back": ...} so the user can check the meaning before sending.
 */
object Translator {

    data class Result(val translation: String, val back: String)

    private fun toneHint(tone: String) = when (tone) {
        "technical" -> "a technical discussion among developers: precise, plain, no fluff"
        "formal" -> "a formal message: polite and well-structured"
        else -> "a casual online chat: relaxed and natural, the way a native speaker types"
    }

    private fun systemPrompt(target: String, tone: String) = """
        You translate the user's message into $target. The context is ${toneHint(tone)}.
        Write it the way a native speaker would actually write it, not word for word.
        Keep these exactly as they are: code, commands, file paths, URLs, @mentions, #hashtags, emoji, and product or project names.
        Keep the line breaks and the overall length similar. Do not add explanations, greetings or quotes.
        If the text is already in $target, just polish it.
        Reply with JSON only: {"translation": "<the $target text>", "back": "<a faithful translation of your $target text back into the original language>"}
    """.trimIndent()

    suspend fun translate(text: String, prefs: TranslatePrefs): Result = withContext(Dispatchers.IO) {
        val key = prefs.apiKey.getValue().trim()
        val base = prefs.baseUrl.getValue().trim().trimEnd('/')
        val url = if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
        val body = JSONObject()
            .put("model", prefs.model.getValue().trim())
            .put("temperature", 0.3)
            .put("response_format", JSONObject().put("type", "json_object"))
            .put(
                "messages", JSONArray()
                    .put(JSONObject().put("role", "system").put("content", systemPrompt(prefs.target.getValue().trim().ifEmpty { "English" }, prefs.tone.getValue())))
                    .put(JSONObject().put("role", "user").put("content", text))
            )
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = "POST"
            c.connectTimeout = 15000
            c.readTimeout = 60000
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.setRequestProperty("Authorization", "Bearer $key")
            c.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = c.responseCode
            val resp = (if (code in 200..299) c.inputStream else c.errorStream)?.use { it.readBytes().toString(Charsets.UTF_8) }.orEmpty()
            if (code !in 200..299) {
                val msg = runCatching { JSONObject(resp).optJSONObject("error")?.optString("message") }.getOrNull()
                throw IOException("HTTP $code${if (!msg.isNullOrBlank()) " · $msg" else ""}")
            }
            val content = JSONObject(resp).getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content").trim()
            // Some providers ignore response_format and wrap the JSON in a code fence, or return plain text
            val json = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            runCatching {
                val o = JSONObject(json)
                Result(o.getString("translation").trim(), o.optString("back").trim())
            }.getOrElse { Result(content, "") }
        } finally {
            c.disconnect()
        }
    }
}
